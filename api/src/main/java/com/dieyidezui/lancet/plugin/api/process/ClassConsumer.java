package com.dieyidezui.lancet.plugin.api.process;

import org.objectweb.asm.tree.ClassNode;

public interface ClassConsumer {

    void accept(ClassNode node);
}
