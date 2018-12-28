package com.dieyidezui.lancet.plugin.api.asm;

import com.dieyidezui.lancet.plugin.api.transform.TransformContext;
import org.objectweb.asm.ClassVisitor;

public class LancetClassVisitorHelper {

    public static void attach(LancetClassVisitor visitor, TransformContext context) {
        visitor.attach(context);
    }

    public static void detach(LancetClassVisitor visitor) {
        visitor.detach();
    }

    public static void link(LancetClassVisitor pre, ClassVisitor next) {
        pre.linkNext(next);
    }
}
