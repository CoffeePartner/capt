package com.dieyidezui.lancet.plugin.api.process;

import com.dieyidezui.lancet.plugin.api.annotations.Meta;
import com.dieyidezui.lancet.plugin.api.asm.LancetClassVisitor;
import com.dieyidezui.lancet.plugin.api.graph.ClassInfo;
import com.dieyidezui.lancet.plugin.api.hint.Thread;
import com.dieyidezui.lancet.plugin.api.hint.Type;
import org.objectweb.asm.MethodVisitor;

import javax.annotation.Nullable;

/**
 * Only class contains plugin interested annotations will pass to MetaProcessor
 */
public abstract class MetaProcessor {
    /**
     * Class changed, pre has {@link Meta}, but current removed.
     *
     * @param basicInfo info
     * @return class consumer
     */
    @Thread(Type.COMPUTATION)
    @Nullable
    public ClassConsumer onMetaRemoved(ClassInfo basicInfo) {
        return null;
    }

    /**
     * Class changed, pre doesn't have {@link Meta}, but current got.
     *
     * @param basicInfo info
     * @return class consumer
     */
    @Thread(Type.COMPUTATION)
    @Nullable
    public ClassConsumer onMetaAdded(ClassInfo basicInfo) {
        return null;
    }

    /**
     * Class changed, both have {@link Meta}
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
}
