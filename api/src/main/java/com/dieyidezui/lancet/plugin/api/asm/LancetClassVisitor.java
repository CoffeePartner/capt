package com.dieyidezui.lancet.plugin.api.asm;

import com.dieyidezui.lancet.plugin.api.TransformContext;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Objects;

public abstract class LancetClassVisitor extends ClassVisitor {

    private TransformContext context;
    private ClassVisitor next;

    public LancetClassVisitor() {
        this(null);
    }

    public LancetClassVisitor(LancetClassVisitor next) {
        super(Opcodes.ASM7, next);
        this.next = next;
    }

    public final void linkNext(LancetClassVisitor next) {
        if (cv != null) {
            if (cv instanceof LancetClassVisitor) {
                ((LancetClassVisitor) cv).linkNext(next);
            } else {
                throw new IllegalStateException("Require LancetClassVisitor or subclass, but is " + cv.getClass().getName());
            }
        } else {
            cv = next;
        }
    }

    final void attach(TransformContext context) {
        this.context = context;
    }

    final void detach() {
        this.context = null;
    }

    protected final TransformContext context() {
        return Objects.requireNonNull(context, "use context() between attach & detach");
    }

    @Override
    public final void visitEnd() {
        if (next != null && next != cv) {
            throw new IllegalStateException("Don't change this.cv by yourself!");
        }
        onVisitEnd();
    }

    /**
     * It will be invoked at last always.
     *
     * @return true if changed anything on the class
     */
    public boolean onVisitEnd() {
        return false;
    }
}
