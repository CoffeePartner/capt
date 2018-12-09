package com.dieyidezui.lancet.core;

import org.objectweb.asm.ClassVisitor;

public abstract class LancetPlugin {

    public void create(PluginContext context) {

    }

    public final int priority() {

    }

    public abstract boolean onCreate(PluginContext context);

    protected abstract LancetClassVisitor onIntercept(String name, ...) {

    }

    public abstract

    public abstract void onEnd(PluginContext context);
}
