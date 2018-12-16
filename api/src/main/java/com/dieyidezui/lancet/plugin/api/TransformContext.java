package com.dieyidezui.lancet.plugin.api;

import org.objectweb.asm.ClassVisitor;

/**
 * TODO: more API
 */
public interface TransformContext {

    ClassVisitor getLastWriter();
}
