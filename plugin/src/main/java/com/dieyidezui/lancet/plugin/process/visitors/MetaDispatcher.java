package com.dieyidezui.lancet.plugin.process.visitors;

import com.android.build.api.transform.TransformException;
import com.dieyidezui.lancet.plugin.api.graph.Status;
import com.dieyidezui.lancet.plugin.api.process.ClassConsumer;
import com.dieyidezui.lancet.plugin.api.process.MetaProcessor;
import com.dieyidezui.lancet.plugin.graph.ApkClassGraph;
import com.dieyidezui.lancet.plugin.graph.ApkClassInfo;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;
import com.dieyidezui.lancet.plugin.resource.VariantResource;
import com.dieyidezui.lancet.plugin.util.Constants;
import com.dieyidezui.lancet.plugin.util.TypeUtil;
import com.dieyidezui.lancet.plugin.util.WaitableTasks;
import com.google.common.io.ByteStreams;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetaDispatcher {

    private static final String META_NAME = TypeUtil.objDescToInternalName(Constants.META);
    private static final Logger LOGGER = Logging.getLogger(MetaDispatcher.class);

    private Map<String, Set<String>> preMetas = Collections.emptyMap();
    private Map<String, Set<String>> metas = new ConcurrentHashMap<>();

    private final GlobalResource global;

    public MetaDispatcher(GlobalResource global) {
        this.global = global;
    }


    @SuppressWarnings("Convert2Lambda")
    public Consumer<MetaClasses> readPreMetas() {
        return new Consumer<MetaClasses>() {
            @Override
            public void accept(MetaClasses m) {
                preMetas = m.classes;
            }
        };
    }

    @SuppressWarnings("Convert2Lambda")
    public Supplier<MetaClasses> writeMetas() {
        return new Supplier<MetaClasses>() {
            @Override
            public MetaClasses get() {
                return new MetaClasses(metas);
            }
        };
    }

    public void addMeta(String meta) {
        metas.put(meta, Collections.emptySet());
    }

    public static class MetaClasses {

        public MetaClasses(Map<String, Set<String>> classes) {
            this.classes = classes;
        }

        public Map<String, Set<String>> classes;
    }


    public void dispatchMetas(boolean incremental, ApkClassGraph graph, VariantResource resource, MetaProcessorFactory factory) throws InterruptedException, TransformException, IOException {
        PerMetaDispatcher inner = new PerMetaDispatcher(factory);
        ForkJoinPool pool = global.computation();
        WaitableTasks tasks = WaitableTasks.get(global.io());

        Set<String> set = new HashSet<>(metas.keySet());
        set.addAll(preMetas.keySet());

        metas.clear();

        for (String className : set) {
            ApkClassInfo info = Objects.requireNonNull(graph.get(className));
            // full mode only return NOT_EXISTS or NOT_CHANGED
            if (info.exists()) {
                if (incremental && info.status() == Status.NOT_CHANGED) {
                    continue;
                }
                tasks.submit(() -> {
                    byte[] classBytes = ByteStreams.toByteArray(resource.openStream(className));
                    ClassReader cr = new ClassReader(classBytes);
                    ClassNode node = new ClassNode();
                    AnnotationCollector collector = new AnnotationCollector(node);
                    cr.accept(collector, 0);
                    Set<String> annotations = collector.annotations();
                    if (!annotations.contains(META_NAME)) {
                        annotations = null;
                    } else {
                        metas.put(className, annotations);
                    }
                    inner.process(pool, info, preMetas.get(className), annotations, node);
                    return null;
                });
            } else if (incremental) {
                // class removed
                inner.process(pool, info, Objects.requireNonNull(preMetas.get(className)), null, null);
            }
        }
        tasks.await();
        pool.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        inner.providers.forEach(i -> pool.execute(() -> i.processor().onProcessEnd()));
        pool.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    static class AnnotationCollector extends ClassVisitor {

        Set<String> annotations = new HashSet<>();

        public AnnotationCollector(ClassVisitor next) {
            super(Opcodes.ASM5, next);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            annotations.add(TypeUtil.objDescToInternalName(descriptor));
            return super.visitAnnotation(descriptor, visible);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            return new FieldVisitor(Opcodes.ASM5, super.visitField(access, name, descriptor, signature, value)) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    annotations.add(TypeUtil.objDescToInternalName(descriptor));
                    return super.visitAnnotation(descriptor, visible);
                }
            };
        }

        Set<String> annotations() {
            return annotations;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return new MethodVisitor(Opcodes.ASM5, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    annotations.add(TypeUtil.objDescToInternalName(descriptor));
                    return super.visitAnnotation(descriptor, visible);
                }

                @Override
                public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
                    annotations.add(TypeUtil.objDescToInternalName(descriptor));
                    return super.visitParameterAnnotation(parameter, descriptor, visible);
                }
            };
        }
    }

    static class PerMetaDispatcher {
        List<MetaProcessorProvider> providers;

        public PerMetaDispatcher(MetaProcessorFactory factory) {
            this.providers = factory.create().collect(Collectors.toList());
        }

        public void process(ForkJoinPool pool, ApkClassInfo info, @Nullable Set<String> pre, @Nullable Set<String> cur, @Nullable ClassNode node) {
            providers.stream()
                    .map(p -> new RecursiveAction() {
                        @Override
                        protected void compute() {
                            MetaProcessor processor = p.processor();
                            Set<String> supported = p.supports();
                            if (!info.exists() && !Collections.disjoint(pre, supported)) {
                                // not exists means: pre has, but cur removed
                                processor.onMetaClassRemoved(info);
                            } else {
                                boolean matchPre = pre != null && !Collections.disjoint(pre, supported);
                                boolean matchCur = cur != null && !Collections.disjoint(cur, supported);
                                switch (info.status()) {
                                    case NOT_CHANGED:
                                        if (matchCur) {
                                            consume(processor.onMetaClassNotChanged(info), node);
                                        }
                                        break;
                                    case ADDED:
                                        if (matchCur) {
                                            consume(processor.onMetaClassAdded(info), node);
                                        }
                                        break;
                                    case CHANGED:
                                        if (matchPre) {
                                            if (matchCur) {
                                                consume(processor.onMetaChanged(info), node);
                                            } else {
                                                consume(processor.onMetaMismatch(info), node);
                                            }
                                        } else if (matchCur) {
                                            consume(processor.onMetaMatched(info), node);
                                        }
                                }
                            }
                        }
                    })
                    .forEach(pool::execute);
        }

        static void consume(@Nullable ClassConsumer consumer, ClassNode node) {
            if (consumer != null) {
                consumer.accept(node);
            }
        }
    }


    public interface MetaProcessorProvider {

        Set<String> supports();

        MetaProcessor processor();
    }

    public interface MetaProcessorFactory {
        Stream<MetaProcessorProvider> create();
    }
}
