package com.dieyidezui.lancet.plugin.api.transform;

import com.dieyidezui.lancet.plugin.api.asm.LancetClassVisitor;
import com.dieyidezui.lancet.plugin.api.graph.ClassInfo;
import com.dieyidezui.lancet.plugin.api.hint.Thread;
import com.dieyidezui.lancet.plugin.api.hint.Type;

import javax.annotation.Nullable;

public interface ClassTransformer {
    /**
     * After parse every meta class, tell lancet which classes require to transform.
     *
     * @return class request
     */
    @Thread(Type.COMPUTATION)
    ClassRequest beforeTransform();

    /**
     * @param classInfo the basic info of class
     * @param required  true if the class  in your ClassRequest
     * @return the class visitor to participate in class transform.
     */
    @Thread(Type.COMPUTATION)
    @Nullable
    LancetClassVisitor onTransform(ClassInfo classInfo, boolean required);

    /**
     * Invoked after all class transform done.
     */
    @Thread(Type.IO)
    void afterTransform();
}
