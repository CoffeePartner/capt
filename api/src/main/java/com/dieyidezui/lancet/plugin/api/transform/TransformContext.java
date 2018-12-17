package com.dieyidezui.lancet.plugin.api.transform;

import org.objectweb.asm.ClassVisitor;

/**
 * TODO: more API
 */
public interface TransformContext {

    /**
     * This usually invoked on visitEnd(), tell lancet if you changed the origin class content.
     */
    void notifyChanged();

    /**
     * @return The last ClassWriter on the chain.
     */
    ClassVisitor getLastWriter();
}
