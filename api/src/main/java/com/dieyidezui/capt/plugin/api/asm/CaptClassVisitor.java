package com.dieyidezui.capt.plugin.api.asm;

import com.dieyidezui.capt.plugin.api.transform.TransformContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Objects;

public abstract class CaptClassVisitor extends ClassVisitor {

    private TransformContext context;

    public CaptClassVisitor() {
        this(null);
    }

    public CaptClassVisitor(@Nullable CaptClassVisitor next) {
        super(Opcodes.ASM5, next);
    }

    protected final TransformContext context() {
        return Objects.requireNonNull(context, "Don't use context() outside visit lifecycle.");
    }

    final void detach() {
        this.context = null;
    }

    final void link(ClassVisitor next) {
        if (cv != null) {
            if (cv instanceof CaptClassVisitor) {
                ((CaptClassVisitor) cv).link(next);
            } else {
                throw new IllegalStateException("Require CaptClassVisitor or subclass, but is " + cv.getClass().getName());
            }
        } else {
            cv = next;
        }
    }

    final Iterator<CaptClassVisitor> iterate() {
        return new Iterator<CaptClassVisitor>() {
            CaptClassVisitor c = CaptClassVisitor.this;

            @Override
            public boolean hasNext() {
                return c != null;
            }

            @Override
            public CaptClassVisitor next() {
                CaptClassVisitor cur = c;
                c = c.cv instanceof CaptClassVisitor ? (CaptClassVisitor) c.cv : null;
                return cur;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    final void attach(TransformContext context) {
        this.context = context;
    }

    /**
     * flags:
     * lower 16bit:
     * {@link ClassReader#SKIP_CODE}
     * {@link ClassReader#SKIP_DEBUG}
     * {@link ClassReader#SKIP_FRAMES}
     * {@link ClassReader#EXPAND_FRAMES}
     * higher 16bit:
     * {@link ClassWriter#COMPUTE_MAXS}
     * {@link ClassWriter#COMPUTE_FRAMES}
     * <p>
     * example: {@code ClassWriter.COMPUTE_MAXS << 16 | ClassReader.EXPAND_FRAMES}
     * Take care, the flags affect every visitor on the chain.
     *
     * @return the required flag
     */
    protected int beforeAttach() {
        return 0;
    }
}
