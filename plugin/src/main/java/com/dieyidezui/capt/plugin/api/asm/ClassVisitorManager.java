package com.dieyidezui.capt.plugin.api.asm;

import com.dieyidezui.capt.plugin.api.transform.TransformContext;
import com.google.common.collect.Streams;
import org.objectweb.asm.ClassVisitor;

import java.util.stream.Stream;

public final class ClassVisitorManager {

    public void link(CaptClassVisitor pre, ClassVisitor next) {
        pre.link(next);
    }

    public void attach(CaptClassVisitor visitor, TransformContext context) {
        visitor.attach(context);
    }

    public Stream<CaptClassVisitor> expand(CaptClassVisitor header) {
        return Streams.stream(header.iterate());
    }

    public int beforeAttach(CaptClassVisitor visitor) {
        return visitor.beforeAttach();
    }

    public void detach(CaptClassVisitor visitor) {
        visitor.detach();
    }
}
