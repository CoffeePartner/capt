package com.dieyidezui.lancet.core;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;

public abstract class LancetClassVisitor extends ClassVisitor {

    private boolean skip = false;

    public LancetClassVisitor() {
        super(Opcodes.ASM5, null);
    }

    protected TransformContext getContext() {

    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public final ModuleVisitor visitModule(String name, int access, String version) {
    }

    @Override
    public final void visitNestHost(String nestHost) {
    }

    @Override
    public final void visitNestMember(String nestMember) {
    }

    @Override
    public final void visitEnd() {
        onEnd();
        super.visitEnd();
    }

    /**
     * It will invoked at last, except someone {@link TransformContext#chooseTo} do something.
     *
     * @return true if changed the class
     */
    protected boolean onEnd() {
        return false;
    }
}
