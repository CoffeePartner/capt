package coffeepartner.capt.plugin.api.transform;

import org.objectweb.asm.ClassVisitor;

/**
 * Context during single class transform.
 * TODO: more API
 */
public interface TransformContext {

    /**
     * This usually invoked on visitEnd(), tell capt if you changed the origin class content.
     */
    void notifyChanged();

    /**
     * @return The last ClassWriter on the chain.
     */
    ClassVisitor getLastWriter();
}
