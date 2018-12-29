package com.dieyidezui.lancet.plugin.process.visitors;

import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.TransformException;
import com.dieyidezui.lancet.plugin.graph.ApkClassGraph;
import com.dieyidezui.lancet.plugin.graph.ClassBean;
import com.dieyidezui.lancet.plugin.graph.MethodBean;
import com.dieyidezui.lancet.plugin.util.ClassWalker;
import com.dieyidezui.lancet.plugin.util.asm.AnnotationSniffer;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class FirstRound {

    private static final Logger LOGGER = Logging.getLogger(FirstRound.class);

    private final ApkClassGraph graph;

    public FirstRound(ApkClassGraph graph) {
        this.graph = graph;
    }

    public void accept(ClassWalker walker, boolean incremental, AnnotationCollector collector) throws IOException, InterruptedException, TransformException {
        walker.visit(incremental, false, asFactory(collector));
    }

    private ClassWalker.Visitor.Factory asFactory(AnnotationCollector collector) {
        return (incremental, content) -> {
            if (incremental && content instanceof JarInput) {
                JarInput j = (JarInput) content;
                if (j.getStatus() == Status.REMOVED) {
                    graph.onJarRemoved(j.getName());
                    return null;
                } else if (j.getStatus() == Status.CHANGED) {
                    graph.onJarRemoved(j.getName());
                }
            }
            return new NamedVisitor(content.getName(), collector);
        };
    }

    public interface AnnotationCollector {
        void onAnnotation(String className, Set<String> annotations);
    }

    class NamedVisitor implements ClassWalker.Visitor {

        private final String name;
        private final AnnotationCollector collector;

        NamedVisitor(String name, AnnotationCollector collector) {
            this.name = name;
            this.collector = collector;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public ForkJoinTask<ClassWalker.ClassEntry> onVisit(ForkJoinPool pool, @Nullable byte[] classBytes, String className, Status status) {
            if (status == Status.REMOVED) {
                graph.markRemoved(className, name);
                return null;
            }
            return pool.submit(() -> {
                try {
                    ClassReader reader = new ClassReader(classBytes);
                    reader.accept(new FirstRoundVisitor(className, status, name, collector), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
                } catch (RuntimeException e) { // class maybe illegal, we catch and ignore it.
                    LOGGER.warn("Class '" + className + "' in " + name + " parse failed, skip it", e);
                }
                return null;
            });
        }
    }

    class FirstRoundVisitor extends AnnotationSniffer {

        private final String expectedName;
        private final Status status;
        private final String belongsTo;
        private final AnnotationCollector collector;
        private ClassBean bean;

        FirstRoundVisitor(String expectedName, Status status, String belongsTo, AnnotationCollector collector) {
            super(null);
            this.expectedName = expectedName;
            this.status = status;
            this.belongsTo = belongsTo;
            this.collector = collector;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            if (!expectedName.equals(name)) {
                throw new IllegalArgumentException("Class name '" + name + "' is not the same as expected '" + expectedName + "' in" + belongsTo);
            }
            bean = new ClassBean(access, name, signature, superName, interfaces);
            bean.belongsTo = belongsTo;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            bean.addMethod(new MethodBean(access, name, descriptor, signature));
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        @Override
        public void visitEnd() {
            graph.add(bean, mapStatus(status));
            collector.onAnnotation(expectedName, annotations());
        }
    }

    static com.dieyidezui.lancet.plugin.api.graph.Status mapStatus(Status status) {
        switch (status) {
            case ADDED:
                return com.dieyidezui.lancet.plugin.api.graph.Status.ADDED;
            case CHANGED:
                return com.dieyidezui.lancet.plugin.api.graph.Status.CHANGED;
            case REMOVED:
                return com.dieyidezui.lancet.plugin.api.graph.Status.REMOVED;
            default:
                return com.dieyidezui.lancet.plugin.api.graph.Status.NOT_CHANGED;
        }
    }
}
