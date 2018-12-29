package com.dieyidezui.lancet.plugin.process.visitors;

import com.android.build.api.transform.*;
import com.dieyidezui.lancet.plugin.api.asm.ClassVisitorManager;
import com.dieyidezui.lancet.plugin.api.asm.LancetClassVisitor;
import com.dieyidezui.lancet.plugin.api.transform.ClassRequest;
import com.dieyidezui.lancet.plugin.api.transform.ClassTransformer;
import com.dieyidezui.lancet.plugin.api.transform.TransformContext;
import com.dieyidezui.lancet.plugin.graph.ApkClassGraph;
import com.dieyidezui.lancet.plugin.graph.ApkClassInfo;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;
import com.dieyidezui.lancet.plugin.resource.VariantResource;
import com.dieyidezui.lancet.plugin.util.ClassWalker;
import com.dieyidezui.lancet.plugin.util.Util;
import com.dieyidezui.lancet.plugin.util.WaitableTasks;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * toRemove is higher than ClassTransformer
 */
public class ThirdRound {
    private static final Logger LOGGER = Logging.getLogger(ThirdRound.class);
    private final VariantResource variantResource;
    private final GlobalResource global;
    private final ApkClassGraph graph;
    private boolean hasAll = false;
    private final ClassVisitorManager manager = new ClassVisitorManager();

    public ThirdRound(VariantResource variantResource, GlobalResource global, ApkClassGraph graph) {
        this.variantResource = variantResource;
        this.global = global;
        this.graph = graph;
    }

    public void accept(boolean incremental, ClassWalker walker, TransformProviderFactory manager, TransformInvocation invocation) throws IOException, InterruptedException, TransformException {
        List<PluginTransform> transforms = manager.create().map(PluginTransform::new).collect(Collectors.toList());

        // 1. call beforeTransform to collect class request
        ForkJoinPool pool = global.computation();
        final WaitableTasks computation = WaitableTasks.get(pool);
        transforms.forEach(c -> computation.execute(c::doRequest));
        computation.await();

        // 2. dispatch classes
        walker.visit(!hasAll && incremental, true, asFactory(transforms));
        if (incremental) {
            // dispatch REMOVED
            graph.getAll().values()
                    .stream()
                    .filter(c -> c.status() == com.dieyidezui.lancet.plugin.api.graph.Status.REMOVED)
                    .forEach(c -> transformForRemoved(pool, transforms, c));
        }
        if (!hasAll && incremental) {
            // dispatch extraSpecified() & re rack for removed plugins
            // directory is simple, jar may cause rewrite the whole jar, take care of it
            Future<Map<QualifiedContent, Set<String>>> future = computation.submit(() -> {
                Map<String, QualifiedContent> inputs = invocation.getInputs().stream()
                        .flatMap(i -> Stream.concat(i.getDirectoryInputs().stream(), i.getJarInputs().stream()))
                        .collect(Collectors.toMap(QualifiedContent::getName, Function.identity()));

                return Stream.concat(
                        manager.collectRemovedPluginsAffectedClasses(graph),
                        transforms.stream()
                                .flatMap(t -> t.extra.stream())
                                .map(graph::get)
                                .filter(Objects::nonNull)
                                .filter(c -> c.status() == com.dieyidezui.lancet.plugin.api.graph.Status.NOT_CHANGED))
                        // ignore to remove
                        .collect(Collectors.groupingBy(s -> inputs.get(s.clazz.belongsTo), HashMap::new,
                                Collector.of(HashSet::new, (s, v) -> s.add(v.name()), (s1, s2) -> {
                                    s1.addAll(s2);
                                    return s1;
                                }, Collector.Characteristics.UNORDERED)));
            });
            walker.visitTargets(asFactory(transforms), Util.await(future));
        } else {
            pool.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }

        // 3. call afterTransform
        WaitableTasks io = WaitableTasks.get(global.io());
        transforms.forEach(t -> io.submit(() -> {
            t.provider.transformer().afterTransform();
            return null;
        }));
        io.await();
    }

    private static void transformForRemoved(ForkJoinPool pool, List<PluginTransform> transforms, ApkClassInfo info) {
        pool.execute(new RecursiveAction() {
            @Override
            protected void compute() {
                invokeAll(transforms.stream().<RecursiveAction>map(c -> new RecursiveAction() {
                    @Override
                    protected void compute() {
                        // just ignore result, because removed class can't be transformed
                        c.createVisitor(info);
                    }
                }).toArray(ForkJoinTask[]::new));
            }
        });
    }

    private ClassWalker.Visitor.Factory asFactory(List<PluginTransform> transforms) {
        return (incremental, content) -> {
            if (incremental && content instanceof JarInput && ((JarInput) content).getStatus() == Status.REMOVED) {
                return null;
            }
            return new TransformVisitor(transforms);
        };
    }


    class TransformVisitor implements ClassWalker.Visitor {

        private final List<PluginTransform> transforms;

        TransformVisitor(List<PluginTransform> transforms) {
            this.transforms = transforms;
        }

        @Nullable
        @Override
        public ForkJoinTask<ClassWalker.ClassEntry> onVisit(ForkJoinPool pool, @Nullable byte[] classBytes, String className, Status status) {
            if (status != Status.REMOVED) {
                return pool.submit(() -> {
                    // retain null to keep the original order map to transform
                    List<LancetClassVisitor> visitors = transforms
                            .stream()
                            .map(t -> t.createVisitor(graph.get(className)))
                            .collect(Collectors.toList());
                    final int flags = visitors.stream().filter(Objects::nonNull).flatMap(manager::expand)
                            .map(manager::beforeAttach)
                            .reduce(0, (l, r) -> l | r);
                    ClassReader cr = new ClassReader(classBytes);

                    int writeFlag = (flags >> 16) & 0xff;
                    ClassWriter cw = ((writeFlag & ClassWriter.COMPUTE_FRAMES) != 0)
                            ? new ComputeFrameClassWriter(cr, writeFlag, variantResource.getFullAndroidLoader())
                            : new ClassWriter(cr, writeFlag);

                    ClassVisitor header = cw;
                    List<LancetClassVisitor> preGroup = null;
                    // link every two expanded group
                    for (int i = 0; i < visitors.size(); i++) {
                        LancetClassVisitor v = visitors.get(i);
                        if (v != null) {
                            TransformContext context = transforms.get(i).makeContext(className, cw);
                            List<LancetClassVisitor> curGroup = manager.expand(v).peek(s -> manager.attach(s, context)).collect(Collectors.toList());
                            if (preGroup == null) {
                                header = curGroup.get(0);
                            } else {
                                manager.link(preGroup.get(preGroup.size() - 1), curGroup.get(0));
                            }
                            preGroup = curGroup;
                        }
                    }

                    // the last group link cw
                    if (preGroup != null) {
                        manager.link(preGroup.get(preGroup.size() - 1), cw);
                    }

                    try {
                        cr.accept(header, flags & 0xff);
                    } catch (RuntimeException e) {
                        LOGGER.warn("Transform class '" + className + "' failed, skip it", e);
                        return new ClassWalker.ClassEntry(className, classBytes);
                    } finally {
                        if (header instanceof LancetClassVisitor) {
                            manager.expand((LancetClassVisitor) header)
                                    .forEach(manager::detach);
                        }
                    }
                    return new ClassWalker.ClassEntry(className, cw.toByteArray());
                });
            }
            return null;
        }
    }

    class PluginTransform {
        final TransformProvider provider;
        ClassRequest request;
        Set<String> extra;

        PluginTransform(TransformProvider provider) {
            this.provider = provider;
        }

        void doRequest() {
            this.request = Objects.requireNonNull(provider.transformer().beforeTransform());
            extra = Objects.requireNonNull(request.extraSpecified());
            if (!hasAll && Objects.requireNonNull(request.scope()) == ClassRequest.Scope.ALL) {
                hasAll = true;
            }
        }

        LancetClassVisitor createVisitor(ApkClassInfo info) {
            boolean target = isTarget(info);
            // we skip the plugin that scope == NONE
            if (!target && request.scope() == ClassRequest.Scope.NONE) {
                return null;
            }
            return provider.transformer().onTransform(info, target);
        }

        boolean isTarget(ApkClassInfo info) {
            switch (request.scope()) {
                case ALL:
                    return true;
                case CHANGED:
                    if (info.status() != com.dieyidezui.lancet.plugin.api.graph.Status.NOT_CHANGED) {
                        return true;
                    }
                default:
                    return extra.contains(info.name());
            }
        }

        TransformContext makeContext(String className, ClassWriter writer) {
            return new TransformContext() {

                @Override
                public void notifyChanged() {
                    provider.onClassAffected(className);
                }

                @Override
                public ClassVisitor getLastWriter() {
                    return writer;
                }
            };
        }
    }

    private static class ComputeFrameClassWriter extends ClassWriter {

        private final URLClassLoader classLoader;

        public ComputeFrameClassWriter(int flags, URLClassLoader classLoader) {
            super(flags);
            this.classLoader = classLoader;
        }

        public ComputeFrameClassWriter(ClassReader classReader, int flags, URLClassLoader classLoader) {
            super(classReader, flags);
            this.classLoader = classLoader;
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            Class<?> c;
            Class<?> d;
            ClassLoader classLoader = this.classLoader;
            try {
                c = Class.forName(type1.replace('/', '.'), false, classLoader);
                d = Class.forName(type2.replace('/', '.'), false, classLoader);
            } catch (Exception e) {
                throw new RuntimeException(
                        String.format(
                                "Unable to find common supper type for %s and %s.", type1, type2),
                        e);
            }
            if (c.isAssignableFrom(d)) {
                return type1;
            }
            if (d.isAssignableFrom(c)) {
                return type2;
            }
            if (c.isInterface() || d.isInterface()) {
                return "java/lang/Object";
            } else {
                do {
                    c = c.getSuperclass();
                } while (!c.isAssignableFrom(d));
                return c.getName().replace('.', '/');
            }
        }
    }


    public interface TransformProvider {

        void onClassAffected(String className);

        ClassTransformer transformer();

    }

    public interface TransformProviderFactory {

        Stream<TransformProvider> create();

        // Rerack classes for removed plugin
        Stream<ApkClassInfo> collectRemovedPluginsAffectedClasses(ApkClassGraph graph);
    }
}
