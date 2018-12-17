package com.dieyidezui.lancet.plugin.api.asm;

import com.dieyidezui.lancet.plugin.api.transform.TransformContext;
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
        this.next = cv;
    }

    final void linkNext(ClassVisitor next) {
        if (cv != null) {
            if (cv instanceof LancetClassVisitor) {
                ((LancetClassVisitor) cv).linkNext(next);
            } else {
                throw new IllegalStateException("Require LancetClassVisitor or subclass, but is " + cv.getClass().getName());
            }
        } else {
            cv = next;
        }
        this.next = cv;
    }

    final void attach(TransformContext context) {
        this.context = context;
    }

    final void detach() {
        this.context = null;
    }

    protected final TransformContext context() {
        return Objects.requireNonNull(context, "Don't use context() outside visit lifecycle.");
    }

    @Override
    public final void visitEnd() {
        if (next != cv) {
            throw new IllegalStateException("Don't change this.cv by yourself!");
        }
        onVisitEnd();
    }

    protected abstract void onVisitEnd();
}
