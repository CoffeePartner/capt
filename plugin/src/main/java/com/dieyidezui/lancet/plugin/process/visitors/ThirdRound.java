package com.dieyidezui.lancet.plugin.process.visitors;

import com.android.build.api.transform.*;
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
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * toRemove is higher than ClassTransformer
 */
public class ThirdRound {
    private final GlobalResource global;
    private final Set<String> toRemove;
    private boolean hasAll = false;

    public ThirdRound(GlobalResource global, Set<String> toRemove) {
        this.global = global;
        this.toRemove = toRemove;
    }

    public void accept(boolean incremental, ClassWalker walker, PluginManager manager, ApkClassGraph graph, TransformInvocation invocation) throws IOException, InterruptedException, TransformException {
        List<PluginTransform> transforms = manager.getProviders().map(PluginTransform::new).collect(Collectors.toList());

        // 1. call beforeTransform to collect class request
        ForkJoinPool pool = global.computation();
        final WaitableTasks computation = WaitableTasks.get(pool);
        transforms.forEach(c -> computation.execute(c::doRequest));
        computation.await();

        // 2. dispatch classes
        walker.visit(!hasAll && incremental, true, asFactory());
        if (incremental) {
            // dispatch REMOVED
            graph.getAll().values()
                    .stream()
                    .filter(c -> c.status() == com.dieyidezui.lancet.plugin.api.graph.Status.REMOVED)
                    .forEach(c -> transforms(pool, transforms, c));

        }
        if (!hasAll && incremental) {
            // dispatch extraSpecified() & re rack for removed plugins
            // directory is simple, jar may cause rewrite the whole jar, take care of it
            Map<String, QualifiedContent> inputs = invocation.getInputs().stream()
                    .flatMap(i -> Stream.concat(i.getDirectoryInputs().stream(), i.getJarInputs().stream()))
                    .collect(Collectors.toMap(QualifiedContent::getName, Function.identity()));

            Map<QualifiedContent, List<ApkClassInfo>> rerack = Stream.concat(manager.collectRemovedPluginsAffectedClasses(graph),
                    transforms.stream()
                            .flatMap(t -> t.extra.stream())
                            .map(graph::get)
                            .filter(Objects::nonNull)
                            .filter(c -> c.status() == com.dieyidezui.lancet.plugin.api.graph.Status.NOT_CHANGED)
            ).distinct().collect(Collectors.groupingBy(s -> inputs.get(s.name()), Collectors.toList()));
        }
        pool.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        // 3. call afterTransform
        WaitableTasks io = WaitableTasks.get(global.io());
        transforms.forEach(t -> io.execute(() -> t.provider.transformer().afterTransform()));
        io.await();
    }

    private static void transforms(ForkJoinPool pool, List<PluginTransform> transforms, ApkClassInfo info) {
        pool.execute(new RecursiveAction() {
            @Override
            protected void compute() {
                invokeAll(transforms.stream().<RecursiveAction>map(c -> new RecursiveAction() {
                    @Override
                    protected void compute() {
                        // just ignore result, because removed class can't be transformed
                        c.callTransform(info);
                    }
                }).toArray(ForkJoinTask[]::new));
            }
        });
    }

    private ClassWalker.Visitor.Factory asFactory() {
        return (incremental, content) -> {
            if (incremental && content instanceof JarInput && ((JarInput) content).getStatus() == Status.REMOVED) {
                return null;
            }
            return new TransformVisitor();
        };
    }


    class TransformVisitor implements ClassWalker.Visitor {

        @Nullable
        @Override
        public ForkJoinTask<ClassWalker.ClassEntry> onVisit(ForkJoinPool pool, @Nullable byte[] classBytes, String className, Status status) {
            if (status != Status.REMOVED && !toRemove.contains(className)) {

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

        public void doRequest() {
            this.request = Objects.requireNonNull(provider.transformer().beforeTransform());
            extra = Objects.requireNonNull(request.extraSpecified());
            if (!hasAll && Objects.requireNonNull(request.scope()) == ClassRequest.Scope.ALL) {
                hasAll = true;
            }
        }

        public LancetClassVisitor callTransform(ApkClassInfo info) {
            return provider.transformer().onTransform(info, isTarget(info));
        }

        public boolean isTarget(ApkClassInfo info) {
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

        public TransformContext makeVisitor(String className, ClassWriter writer) {
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
