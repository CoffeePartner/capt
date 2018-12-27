package com.dieyidezui.lancet.plugin.api.process;

import com.dieyidezui.lancet.rt.annotations.Meta;
import com.dieyidezui.lancet.plugin.api.graph.ClassInfo;
import com.dieyidezui.lancet.plugin.api.hint.Thread;
import com.dieyidezui.lancet.plugin.api.hint.Type;

import javax.annotation.Nullable;

/**
 * Only class contains plugin interested annotations will pass to MetaProcessor
 */
public abstract class MetaProcessor {
    /**
     * Class changed, pre matched, but current mismatched, due to one of the following:
     * 1.  @Meta removed from class
     * 2.  No matched annotation in @Def.
     *
     * @param basicInfo info
     * @return class consumer
     */
    @Thread(Type.COMPUTATION)
    @Nullable
    public ClassConsumer onMetaMismatch(ClassInfo basicInfo) {
        return null;
    }

    /**
     * Class changed, pre doesn't match, but current does.
     *
     * @param basicInfo info
     * @return class consumer
     */
    @Thread(Type.COMPUTATION)
    @Nullable
    public ClassConsumer onMetaMatched(ClassInfo basicInfo) {
        return null;
    }

    /**
     * Class changed, both matched.
     *
     * @param basicInfo info
     * @return class consumer
     */
    @Thread(Type.COMPUTATION)
    @Nullable
    public ClassConsumer onMetaChanged(ClassInfo basicInfo) {
        return null;
    }

    /**
     * Class removed, pre has {@link Meta}.
     *
     * @param basicInfo last info
     */
    @Thread(Type.COMPUTATION)
    public void onMetaClassRemoved(ClassInfo basicInfo) {
    }

    /**
     * Class added, current has {@link Meta}.
     *
     * @param basicInfo
     * @return class consumer
     */
    @Thread(Type.COMPUTATION)
    @Nullable
    public ClassConsumer onMetaClassAdded(ClassInfo basicInfo) {
        return null;
    }

    /**
     * Class not changed, both have {@link Meta}.
     * If we not in incremental mode, we will call this method only.
     *
     * @param basicInfo info
     * @return class consumer
     */
    @Thread(Type.COMPUTATION)
    @Nullable
    public ClassConsumer onMetaClassNotChanged(ClassInfo basicInfo) {
        return null;
    }

    @Thread(Type.COMPUTATION)
    public void onProcessEnd() {
    }
}
