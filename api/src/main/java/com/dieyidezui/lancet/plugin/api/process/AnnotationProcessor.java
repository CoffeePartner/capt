package com.dieyidezui.lancet.plugin.api.process;

import com.dieyidezui.lancet.plugin.api.graph.ClassInfo;
import com.dieyidezui.lancet.plugin.api.hint.Thread;
import com.dieyidezui.lancet.plugin.api.hint.Type;

import javax.annotation.Nullable;

/**
 * Only class contains plugin interested annotations will pass to AnnotationProcessor
 */
public abstract class AnnotationProcessor {
    /**
     * Class changed, pre matched, but current mismatched, due to one of the following:
     * 1.  No matched annotation in @Def.
     *
     * @param info info
     * @return class consumer
     */
    @Thread(Type.COMPUTATION)
    @Nullable
    public ClassConsumer onAnnotationMismatch(ClassInfo info) {
        return null;
    }

    /**
     * Class changed, pre doesn't match, but current does.
     *
     * @param info info
     * @return class consumer
     */
    @Thread(Type.COMPUTATION)
    @Nullable
    public ClassConsumer onAnnotationMatched(ClassInfo info) {
        return null;
    }

    /**
     * Class changed, both matched.
     *
     * @param info info
     * @return class consumer
     */
    @Thread(Type.COMPUTATION)
    @Nullable
    public ClassConsumer onAnnotationChanged(ClassInfo info) {
        return null;
    }

    /**
     * Class removed, pre matched.
     *
     * @param info last info
     */
    @Thread(Type.COMPUTATION)
    public void onAnnotationClassRemoved(ClassInfo info) {
    }

    /**
     * Class added, current matched.
     *
     * @param info info
     * @return class consumer
     */
    @Thread(Type.COMPUTATION)
    @Nullable
    public ClassConsumer onAnnotationClassAdded(ClassInfo info) {
        return null;
    }

    /**
     * Class not changed, both matched.
     * If we not in incremental mode, we will call this method only.
     *
     * @param info info
     * @return class consumer
     */
    @Thread(Type.COMPUTATION)
    @Nullable
    public ClassConsumer onAnnotationClassNotChanged(ClassInfo info) {
        return null;
    }

    @Thread(Type.COMPUTATION)
    public void onProcessEnd() {
    }
}
