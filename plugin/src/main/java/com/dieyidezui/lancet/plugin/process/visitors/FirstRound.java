package com.dieyidezui.lancet.plugin.process.visitors;

import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.dieyidezui.lancet.plugin.graph.ApkClassGraph;
import com.dieyidezui.lancet.plugin.graph.ClassBean;
import com.dieyidezui.lancet.plugin.graph.MethodBean;
import com.dieyidezui.lancet.plugin.util.ClassWalker;
import com.dieyidezui.lancet.plugin.util.ConcurrentHashSet;
import com.dieyidezui.lancet.plugin.util.TypeUtil;
import com.dieyidezui.lancet.rt.annotations.Meta;
import com.dieyidezui.lancet.rt.annotations.RemoveWhenTransform;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.objectweb.asm.*;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class FirstRound implements ClassWalker.Visitor.Factory {

    private static final Logger LOGGER = Logging.getLogger(FirstRound.class);
    private static final String META = Type.getDescriptor(Meta.class);
    private static final String REMOVE = Type.getDescriptor(RemoveWhenTransform.class);

    private final ApkClassGraph graph;
    private final MetaDispatcher metaDispatcher;
    private Set<String> toRemoveWhenTransform = new ConcurrentHashSet<>();

    public FirstRound(ApkClassGraph graph, MetaDispatcher metaDispatcher) {
        this.graph = graph;
        this.metaDispatcher = metaDispatcher;
    }

    @Nullable
    @Override
    public ClassWalker.Visitor newVisitor(boolean incremental, QualifiedContent content) {
        if (incremental && content instanceof JarInput) {
            JarInput j = (JarInput) content;
            if (j.getStatus() == Status.REMOVED || j.getStatus() == Status.CHANGED) {
                graph.onJarRemoved(j.getName());
            }
            return null;
        }
        return new NamedVisitor(content.getName());
    }

    public Set<String> getToRemove() {
        return toRemoveWhenTransform;
    }

    class NamedVisitor implements ClassWalker.Visitor {

        private final String name;

        NamedVisitor(String name) {
            this.name = name;
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
                    reader.accept(new FirstRoundVisitor(className, status, name), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
                } catch (RuntimeException e) { // class maybe illegal, we catch and ignore it.
                    LOGGER.warn("Class '" + className + "' in " + name + "parse failed, skip it", e);
                }
                return null;
            });
        }
    }

    class FirstRoundVisitor extends ClassVisitor {

        private final String expectedName;
        private final Status status;
        private final String belongsTo;
        private ClassBean bean;

        FirstRoundVisitor(String expectedName, Status status, String belongsTo) {
            super(Opcodes.ASM7, null);
            this.expectedName = expectedName;
            this.status = status;
            this.belongsTo = belongsTo;
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
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (META.equals(descriptor)) {
                metaDispatcher.addMeta(TypeUtil.objDescToInternalName(descriptor));
            } else if (REMOVE.equals(descriptor)) {
                toRemoveWhenTransform.add(TypeUtil.objDescToInternalName(descriptor));
            }
            return null;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            bean.addMethod(new MethodBean(access, name, descriptor, signature));
            return null;
        }

        @Override
        public void visitEnd() {
            graph.add(bean, mapStatus(status));
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
