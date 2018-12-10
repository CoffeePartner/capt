package com.dieyidezui.lancet.core;

import com.dieyidezui.lancet.core.graph.ClassInfo;
import org.objectweb.asm.ClassVisitor;

/**
 * onCreate Context incremental
 * onGraphReady Graph
 * run MetaProcessor => Inputs
 * run ClassVisitor
 */
public abstract class LancetPlugin {

    private int priority;

    public void create(PluginContext context) {
        priority = 0;
        // TODO
    }

    public abstract boolean onCreate(PluginContext context);

    public final int priority() {
        return priority;
    }

    public abstract MetaProcessor toProcessor();

    public abstract ClassTransformer toTransformer();

    public abstract ClassVisitor rerack(ClassInfo basicInfo);

    public abstract ClassVisitor specific(ClassInfo basicInfo);

    public abstract void onEnd(PluginContext context);
}
