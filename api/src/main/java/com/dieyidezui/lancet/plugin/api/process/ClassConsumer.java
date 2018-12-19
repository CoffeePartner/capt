package com.dieyidezui.lancet.plugin.api.process;

import com.dieyidezui.lancet.plugin.api.hint.Thread;
import com.dieyidezui.lancet.plugin.api.hint.Type;
import org.objectweb.asm.tree.ClassNode;

public interface ClassConsumer {

    @Thread(Type.COMPUTATION)
    void accept(ClassNode node);
}
