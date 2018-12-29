package com.dieyidezui.lancet.plugin.api.asm;

import com.dieyidezui.lancet.plugin.api.transform.TransformContext;
import com.google.common.collect.Streams;
import org.objectweb.asm.ClassVisitor;

import java.util.stream.Stream;

public final class ClassVisitorManager {

    public void link(LancetClassVisitor pre, ClassVisitor next) {
        pre.link(next);
    }

    public void attach(LancetClassVisitor visitor, TransformContext context) {
        visitor.attach(context);
    }

    public Stream<LancetClassVisitor> expand(LancetClassVisitor header) {
        return Streams.stream(header.iterate());
    }

    public int beforeAttach(LancetClassVisitor visitor) {
        return visitor.beforeAttach();
    }

    public void detach(LancetClassVisitor visitor) {
        visitor.detach();
    }
}
