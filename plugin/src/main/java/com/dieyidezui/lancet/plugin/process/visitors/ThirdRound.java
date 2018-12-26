package com.dieyidezui.lancet.plugin.process.visitors;

import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.TransformException;
import com.dieyidezui.lancet.plugin.api.transform.ClassTransformer;
import com.dieyidezui.lancet.plugin.api.transform.TransformContext;
import com.dieyidezui.lancet.plugin.graph.ApkClassGraph;
import com.dieyidezui.lancet.plugin.process.PluginManager;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;
import com.dieyidezui.lancet.plugin.util.ClassWalker;
import com.dieyidezui.lancet.plugin.util.WaitableTasks;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

/**
 * toRemove is higher than ClassTransformer.
 */
public class ThirdRound {
    private final GlobalResource global;
    private final Set<String> toRemove;

    public ThirdRound(GlobalResource global, Set<String> toRemove) {
        this.global = global;
        this.toRemove = toRemove;
    }

    public void accept(boolean incremental, ClassWalker walker, PluginManager manager, ApkClassGraph graph) throws IOException, InterruptedException, TransformException {

        WaitableTasks tasks = WaitableTasks.get(global.computation());

        tasks.submit();

        // 1. call beforeTransform to collect class request


        // 2. dispatch classes

        if (!incremental) {
            walker.visit(false, true, asFactory());
        } else {
            // dispatch classes without REMOVED
            walker.visit(true, true, asFactory());

            // dispatch REMOVED
            graph.collectRemovedClasses(); // tell the removed classes, but don't do transform

            // dispatch extraSpecified() & re-racked

            // directory is simple, jar may cause rewrite the whole jar, take care of it
            manager.collectRemovedPluginsAffectedClasses(graph);
        }


        // 4. call afterTransform
    }

    private ClassWalker.Visitor.Factory asFactory() {
        return (incremental, content) -> {
            if (incremental && content instanceof JarInput && ((JarInput) content).getStatus() == Status.REMOVED) {
                return null;
            }
            return null;
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

    public static class PluginTransform {
        private final PluginProvider provider;

        public PluginTransform(PluginProvider provider) {
            this.provider = provider;
        }

        public TransformContext makeContext(String className, ClassWriter writer) {
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
