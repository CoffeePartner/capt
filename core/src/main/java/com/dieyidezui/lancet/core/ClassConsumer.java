package com.dieyidezui.lancet.core;

import org.objectweb.asm.tree.ClassNode;

public interface ClassConsumer {

    void accept(ClassNode node);
}
