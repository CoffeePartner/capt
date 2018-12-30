package com.dieyidezui.capt.plugin.api.process;

import com.dieyidezui.capt.plugin.api.hint.Thread;
import com.dieyidezui.capt.plugin.api.hint.Type;
import org.objectweb.asm.tree.ClassNode;

public interface ClassConsumer {

    @Thread(Type.COMPUTATION)
    void accept(ClassNode node);
}
