package com.dieyidezui.lancet.plugin.api.asm;

import com.dieyidezui.lancet.plugin.api.transform.TransformContext;

public class LancetClassVisitorHelper {

    public static void attach(LancetClassVisitor visitor, TransformContext context) {
        visitor.attach(context);
    }

    public static void detach(LancetClassVisitor visitor) {
        visitor.detach();
    }
}
