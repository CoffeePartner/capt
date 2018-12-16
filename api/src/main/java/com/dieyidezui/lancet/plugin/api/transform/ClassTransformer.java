package com.dieyidezui.lancet.plugin.api.transform;

import com.dieyidezui.lancet.plugin.api.asm.LancetClassVisitor;
import com.dieyidezui.lancet.plugin.api.graph.ClassInfo;

import javax.annotation.Nullable;

public interface ClassTransformer {
    /**
     * After parse every meta class, tell lancet which classes require to transform.
     *
     * @return class request
     */
    ClassRequest beforeTransform();

    @Nullable
    LancetClassVisitor onTransform(ClassInfo classInfo, boolean required);


    void afterTransform();
}
