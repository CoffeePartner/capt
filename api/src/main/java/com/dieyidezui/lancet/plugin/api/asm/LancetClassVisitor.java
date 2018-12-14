package com.dieyidezui.lancet.plugin.api.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

public abstract class LancetClassVisitor extends ClassVisitor {

    private boolean skip = false;

    protected ClassVisitor pre;

    public LancetClassVisitor() {
        super(Opcodes.ASM5, null);
    }


    void linkPre(ClassVisitor visitor) {
        pre = visitor;
    }

    void linkNext(ClassVisitor visitor) {
        cv = visitor;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public final ModuleVisitor visitModule(String name, int access, String version) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void visitNestHost(String nestHost) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void visitNestMember(String nestMember) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void visitEnd() {
        throw new UnsupportedOperationException();
    }

    /**
     * It will be invoked at last always.
     * @return true if changed anything on the class
     */
    public boolean onEnd() {
        return false;
    }
}
