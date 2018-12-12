package com.dieyidezui.lancet.plugin.api;

import org.objectweb.asm.tree.ClassNode;

public interface ClassConsumer {

    void accept(ClassNode node);
}
