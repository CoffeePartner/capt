package com.dieyidezui.lancet.plugin.api;

import com.dieyidezui.lancet.plugin.api.annotations.Def;
import com.dieyidezui.lancet.plugin.api.hint.Thread;
import com.dieyidezui.lancet.plugin.api.hint.Type;
import com.dieyidezui.lancet.plugin.api.process.MetaProcessor;
import com.dieyidezui.lancet.plugin.api.transform.ClassTransformer;
import com.dieyidezui.lancet.rt.annotations.Meta;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Lifecycle:
 * 1. beforeCreate in priority order
 * 2. onCreate concurrently
 * 3. Parse classes with {@link Meta} by {@link MetaProcessor}
 * 4. Transform every class from inputs by {@link ClassTransformer}
 * 5. onDestroy concurrently
 */
public abstract class Plugin<T> {

    public final Set<String> getSupportedAnnotations() {
        return arrayToSet(getDef().supportedAnnotationTypes());
    }

    private Def getDef() {
        return Objects.requireNonNull(getClass().getAnnotation(Def.class), "Require @Def on " + getClass().getName());
    }

    public final int defaultPriority() {
        return getDef().defaultPriority();
    }

    /**
     * Apply the plugin, don't do too much work(such as read / write file) in this function.
     *
     * @param lancet Plugin context
     */
    @Thread(Type.SINGLE)
    public void beforeCreate(T lancet) {
    }

    /**
     * Do your time-consuming tasks in this function
     *
     * @param lancet Plugin context
     */
    @Thread(Type.IO)
    public abstract void onCreate(T lancet);

    /**
     * @return meta processor
     */
    @Thread(Type.SINGLE)
    @Nullable
    public MetaProcessor onProcessAnnotations() {
        return null;
    }

    /**
     * @return
     */
    @Thread(Type.SINGLE)
    @Nullable
    public ClassTransformer onTransformClass() {
        return null;
    }

    /**
     * The last time to do your stuff.
     *
     * @param lancet Plugin context
     */
    @Thread(Type.IO)
    public void onDestroy(T lancet) {
    }

    private static Set<String> arrayToSet(String[] array) {
        assert array != null;
        Set<String> set = new HashSet<String>(array.length);
        for (String s : array)
            set.add(s);
        return Collections.unmodifiableSet(set);
    }
}
