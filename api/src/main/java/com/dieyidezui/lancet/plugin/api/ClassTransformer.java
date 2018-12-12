package com.dieyidezui.lancet.plugin.api;

import com.dieyidezui.lancet.plugin.api.graph.ClassInfo;

import javax.annotation.Nullable;

public abstract class ClassTransformer {

    @Nullable
    LancetClassVisitor newVisitor(ClassInfo info, boolean reracked) {
        return null;
    }
}
