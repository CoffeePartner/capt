package com.dieyidezui.capt.plugin.api;

import com.dieyidezui.capt.plugin.api.annotations.Def;
import com.dieyidezui.capt.plugin.api.hint.Thread;
import com.dieyidezui.capt.plugin.api.hint.Type;
import com.dieyidezui.capt.plugin.api.process.AnnotationProcessor;
import com.dieyidezui.capt.plugin.api.transform.ClassTransformer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Lifecycle:
 * 1. beforeCreate in priority order
 * 2. onCreate concurrently
 * 3. Parse classes by {@link AnnotationProcessor}
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
     * @param capt Plugin context
     */
    @Thread(Type.SINGLE)
    public void beforeCreate(T capt) {
    }

    /**
     * Do your time-consuming tasks in this function
     *
     * @param capt Plugin context
     * @throws IOException io
     * @throws InterruptedException inter
     */
    @Thread(Type.IO)
    public abstract void onCreate(T capt) throws IOException, InterruptedException ;

    /**
     * @return meta processor
     */
    @Thread(Type.SINGLE)
    @Nullable
    public AnnotationProcessor onProcessAnnotations() {
        return null;
    }

    /**
     * @return transformer
     */
    @Thread(Type.SINGLE)
    @Nullable
    public ClassTransformer onTransformClass() {
        return null;
    }

    /**
     * The last time to do your stuff.
     *
     * @param capt Plugin context
     * @throws IOException io
     * @throws InterruptedException inter
     */
    @Thread(Type.IO)
    public void onDestroy(T capt) throws IOException, InterruptedException {
    }

    private static Set<String> arrayToSet(String[] array) {
        assert array != null;
        Set<String> set = new HashSet<String>(array.length);
        for (String s : array)
            set.add(s.replace('.', '/'));
        return Collections.unmodifiableSet(set);
    }
}
