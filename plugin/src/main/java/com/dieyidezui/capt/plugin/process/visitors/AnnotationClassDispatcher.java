package com.dieyidezui.capt.plugin.process.visitors;

import com.android.build.api.transform.TransformException;
import com.dieyidezui.capt.plugin.api.graph.Status;
import com.dieyidezui.capt.plugin.api.process.AnnotationProcessor;
import com.dieyidezui.capt.plugin.api.process.ClassConsumer;
import com.dieyidezui.capt.plugin.graph.ApkClassGraph;
import com.dieyidezui.capt.plugin.graph.ApkClassInfo;
import com.dieyidezui.capt.plugin.resource.GlobalResource;
import com.dieyidezui.capt.plugin.resource.VariantResource;
import com.dieyidezui.capt.plugin.util.Util;
import com.dieyidezui.capt.plugin.util.WaitableTasks;
import com.dieyidezui.capt.plugin.util.asm.AnnotationSniffer;
import com.google.common.io.ByteStreams;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnnotationClassDispatcher {
    private static final Logger LOGGER = Logging.getLogger(AnnotationClassDispatcher.class);

    private Map<String, Set<String>> preMatched = Collections.emptyMap();
    private Map<String, Set<String>> matched = new ConcurrentHashMap<>();

    private final GlobalResource global;

    public AnnotationClassDispatcher(GlobalResource global) {
        this.global = global;
    }


    @SuppressWarnings("Convert2Lambda")
    public Consumer<MatchedClasses> readPreMatched() {
        return new Consumer<MatchedClasses>() {
            @Override
            public void accept(MatchedClasses m) {
                preMatched = m.classes;
            }
        };
    }

    @SuppressWarnings("Convert2Lambda")
    public Supplier<MatchedClasses> writeMatched() {
        return new Supplier<MatchedClasses>() {
            @Override
            public MatchedClasses get() {
                return new MatchedClasses(matched);
            }
        };
    }

    public FirstRound.AnnotationCollector toCollector(Set<String> targets) {
        return (className, annotations) -> {
            if (!Collections.disjoint(targets, annotations)) {
                matched.put(className, Collections.emptySet());
            }
        };
    }

    public static class MatchedClasses {

        public MatchedClasses(Map<String, Set<String>> classes) {
            this.classes = classes;
        }

        public Map<String, Set<String>> classes;
    }


    public void dispatch(boolean recompute, boolean incremental, ApkClassGraph graph, VariantResource resource, AnnotationProcessorFactory factory) throws InterruptedException, TransformException, IOException {
        PerClassDispatcher inner = new PerClassDispatcher(factory);
        ForkJoinPool pool = global.computation();
        WaitableTasks tasks = WaitableTasks.get(global.io());

        Set<String> set = new HashSet<>(matched.keySet());
        set.addAll(preMatched.keySet());

        matched.clear();

        for (String className : set) {
            ApkClassInfo info = Objects.requireNonNull(graph.get(className));
            // full mode only return NOT_EXISTS or NOT_CHANGED
            if (info.exists()) {
                if (incremental && info.status() == Status.NOT_CHANGED) {
                    Set<String> annos = preMatched.get(className);
                    if (!recompute || inner.checkStillMatched(annos)) {
                        matched.put(className, annos);
                    }
                    continue;
                }
                tasks.submit(() -> {
                    byte[] classBytes = ByteStreams.toByteArray(resource.openStream(className));
                    ClassReader cr = new ClassReader(classBytes);
                    ClassNode node = new ClassNode();
                    AnnotationSniffer collector = new AnnotationSniffer(node);
                    cr.accept(collector, 0);
                    Set<String> annotations = collector.annotations();

                    matched.put(className, annotations);
                    inner.process(pool, info, preMatched.get(className), annotations, node);
                    return null;
                });
            } else if (incremental) {
                // class removed
                inner.process(pool, info, Objects.requireNonNull(preMatched.get(className)), null, null);
            }
        }
        tasks.await();
        pool.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        inner.providers.forEach(i -> pool.execute(() -> i.processor().onProcessEnd()));
        pool.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    class PerClassDispatcher {
        List<AnnotationProcessorProvider> providers;
        Set<String> allSupported = null;

        PerClassDispatcher(AnnotationProcessorFactory factory) {
            this.providers = factory.create().collect(Collectors.toList());
        }

        public void process(ForkJoinPool pool, ApkClassInfo info, @Nullable Set<String> pre, @Nullable Set<String> cur, @Nullable ClassNode node) {
            pool.submit(() -> {
                boolean hasTrue = false;
                final ThreadLocal<ClassNode> local = ThreadLocal.withInitial(() -> Util.clone(node));
                for (Future<Boolean> future : ForkJoinTask.invokeAll(providers.stream()
                        .map(p -> new RecursiveTask<Boolean>() {
                            @Override
                            protected Boolean compute() {
                                AnnotationProcessor processor = p.processor();

                                Set<String> supported = p.supports();
                                if (!info.exists() && !Collections.disjoint(pre, supported)) {
                                    // not exists means: pre has, but cur removed
                                    processor.onAnnotationClassRemoved(info);
                                } else {
                                    boolean matchPre = pre != null && !Collections.disjoint(pre, supported);
                                    boolean matchCur = cur != null && !Collections.disjoint(cur, supported);
                                    switch (info.status()) {
                                        case NOT_CHANGED:
                                            if (matchCur) {
                                                consume(processor.onAnnotationClassNotChanged(info), local);
                                            }
                                            break;
                                        case ADDED:
                                            if (matchCur) {
                                                consume(processor.onAnnotationClassAdded(info), local);
                                            }
                                            break;
                                        case CHANGED:
                                            if (matchPre) {
                                                if (matchCur) {
                                                    consume(processor.onAnnotationChanged(info), local);
                                                } else {
                                                    consume(processor.onAnnotationMismatch(info), local);
                                                    return false;
                                                }
                                            } else if (matchCur) {
                                                consume(processor.onAnnotationMatched(info), local);
                                            } else {
                                                throw new AssertionError();
                                            }
                                    }
                                }
                                return true;
                            }
                        })
                        .collect(Collectors.toList()))) {
                    hasTrue |= Util.await(future);
                }

                local.remove();
                if (!hasTrue) {
                    //should remove it from map if all mismatch
                    matched.remove(info.name());
                }
                return null;
            });
        }

        public boolean checkStillMatched(Set<String> annos) {
            Set<String> all = allSupported;
            if (all == null) {
                allSupported = all = providers.stream().flatMap(c -> c.supports().stream()).collect(Collectors.toSet());
            }
            return !Collections.disjoint(all, annos);
        }
    }

    static void consume(@Nullable ClassConsumer consumer, ThreadLocal<ClassNode> local) {
        if (consumer != null) {
            consumer.accept(local.get());
        }
    }


    public interface AnnotationProcessorProvider {

        Set<String> supports();

        AnnotationProcessor processor();
    }

    public interface AnnotationProcessorFactory {
        Stream<AnnotationProcessorProvider> create();
    }
}
