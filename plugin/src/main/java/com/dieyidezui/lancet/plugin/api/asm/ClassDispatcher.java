package com.dieyidezui.lancet.plugin.api.asm;

import org.objectweb.asm.ClassVisitor;

public class ClassDispatcher extends ClassVisitor {
    public ClassDispatcher(int api) {
        super(api);
    }
}
