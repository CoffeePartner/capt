package com.dieyidezui.lancet.plugin.process.visitors;

import com.android.build.api.transform.*;
import com.dieyidezui.lancet.plugin.api.asm.ClassVisitorManager;
import com.dieyidezui.lancet.plugin.api.asm.LancetClassVisitor;
import com.dieyidezui.lancet.plugin.api.transform.ClassRequest;
import com.dieyidezui.lancet.plugin.api.transform.ClassTransformer;
import com.dieyidezui.lancet.plugin.api.transform.TransformContext;
import com.dieyidezui.lancet.plugin.graph.ApkClassGraph;
import com.dieyidezui.lancet.plugin.graph.ApkClassInfo;
import com.dieyidezui.lancet.plugin.process.PluginManager;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;
import com.dieyidezui.lancet.plugin.util.ClassWalker;
import com.dieyidezui.lancet.plugin.util.WaitableTasks;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import javax.annotation.Nullable;
import java.io.IOException;
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
    private final GlobalResource global;
    private final Set<String> toRemove;
    private final ApkClassGraph graph;
    private boolean hasAll = false;
    ClassVisitorManager manager = new ClassVisitorManager();

    public ThirdRound(GlobalResource global, Set<String> toRemove, ApkClassGraph graph) {
        this.global = global;
        this.toRemove = toRemove;
        this.graph = graph;
    }

    public void accept(boolean incremental, ClassWalker walker, PluginManager manager, TransformInvocation invocation) throws IOException, InterruptedException, TransformException {
        List<PluginTransform> transforms = manager.getProviders().map(PluginTransform::new).collect(Collectors.toList());

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
                        .filter(c -> !toRemove.contains(c.name()))
                        .collect(Collectors.groupingBy(s -> inputs.get(s.clazz.belongsTo), HashMap::new,
                                Collector.of(HashSet::new, (s, v) -> s.add(v.name()), (s1, s2) -> {
                                    s1.addAll(s2);
                                    return s1;
                                }, Collector.Characteristics.UNORDERED)));
            });
            try {
                walker.visitTargets(asFactory(transforms), future.get());
            } catch (ExecutionException e) {
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                }
                throw new TransformException(e.getCause());
            }
        } else {
            pool.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }

        // 3. call afterTransform
        WaitableTasks io = WaitableTasks.get(global.io());
        transforms.forEach(t -> io.execute(() -> t.provider.transformer().afterTransform()));
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
            if (status != Status.REMOVED && !toRemove.contains(className)) {
                pool.submit(() -> {
                    ClassReader cr = new ClassReader(classBytes);
                    ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS); // just compute max, it may cause 10% slower
                    List<LancetClassVisitor> visitors = transforms
                            .stream()
                            .map(t -> {
                                LancetClassVisitor visitor = t.createVisitor(graph.get(className));
                                if (visitor != null) {
                                    manager.attach(visitor, t.makeContext(className, cw));
                                }
                                return visitor;
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    try {
                        cr.accept(manager.link(visitors, cw), 0);
                    } catch (RuntimeException e) {
                        LOGGER.warn("Transform class '" + className + "' failed, skip it", e);
                        return new ClassWalker.ClassEntry(className, classBytes);
                    }
                    return new ClassWalker.ClassEntry(className, cw.toByteArray());
                });
            }
            return null;
        }
    }

    class PluginTransform {
        final PluginProvider provider;
        ClassRequest request;
        Set<String> extra;

        PluginTransform(PluginProvider provider) {
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
                boolean once = false;

                @Override
                public void notifyChanged() {
                    if (!once) {
                        once = true;
                        provider.onClassAffected(className);
                    }
                }

                @Override
                public ClassVisitor getLastWriter() {
                    return writer;
                }
            };
        }
    }


    public interface PluginProvider {

        void onClassAffected(String className);

        ClassTransformer transformer();
    }
}
