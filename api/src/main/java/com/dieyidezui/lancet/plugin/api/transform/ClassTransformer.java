package com.dieyidezui.lancet.plugin.api.transform;

import com.dieyidezui.lancet.plugin.api.asm.LancetClassVisitor;
import com.dieyidezui.lancet.plugin.api.graph.ClassInfo;
import com.dieyidezui.lancet.plugin.api.hint.Thread;
import com.dieyidezui.lancet.plugin.api.hint.Type;

import javax.annotation.Nullable;
import java.io.IOException;

public abstract class ClassTransformer {
    /**
     * After parse every meta class, tell lancet which classes require to transform.
     *
     * @return class request
     */
    @Thread(Type.COMPUTATION)
    public abstract ClassRequest beforeTransform();

    /**
     * @param classInfo the basic info of class
     * @param required  true if the class  in your ClassRequest
     * @return the class visitor to participate in class transform.
     */
    @Thread(Type.COMPUTATION)
    @Nullable
    public abstract LancetClassVisitor onTransform(ClassInfo classInfo, boolean required);

    /**
     * Invoked after all class transform done.
     * @throws IOException io
     * @throws InterruptedException inter
     */
    @Thread(Type.IO)
    public void afterTransform() throws IOException, InterruptedException {
    }
}
