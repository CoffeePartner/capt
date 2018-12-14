package com.dieyidezui.lancet.plugin.api.transform;

import com.dieyidezui.lancet.plugin.api.asm.LancetClassVisitor;
import com.dieyidezui.lancet.plugin.api.graph.ClassInfo;

import javax.annotation.Nullable;

public abstract class ClassTransformer {

    @Nullable
    LancetClassVisitor newVisitor(ClassInfo info, boolean reracked) {
        return null;
    }
}
