package com.dieyidezui.lancet.core;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import javax.annotation.Nullable;

public abstract class LancetClassVisitor extends ClassVisitor {

    private boolean skip = false;

    public LancetClassVisitor(int api) {
        super(api, null);
    }

    public final void linkTo(@Nullable ClassVisitor next) {
        this.cv = next;
    }

    public void skipClass() {
        skip = true;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitNestHost(String nestHost) {
        super.visitNestHost(nestHost);
    }
}
