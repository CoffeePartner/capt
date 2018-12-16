package com.dieyidezui.lancet.plugin.api;

import com.dieyidezui.lancet.plugin.api.annotations.Meta;
import com.dieyidezui.lancet.plugin.api.process.MetaProcessor;
import com.dieyidezui.lancet.plugin.api.transform.ClassTransformer;

/**
 * Lifecycle:
 * 1. beforeCreate in priority order
 * 2. onCreate concurrently
 * 3. Parse classes with {@link Meta} by {@link MetaProcessor}
 * 4. Transform every class from inputs by {@link ClassTransformer}
 * 5. onDestroy concurrently
 */
public interface Plugin {

    /**
     * Apply the plugin, don't do too much work(such as read / write file) in this function.
     *
     * @param lancet
     */
    void beforeCreate(Lancet lancet);

    /**
     * Do your time-consuming tasks in this function
     */
    void onCreate(Lancet lancet);

    void beforeTransform();

    void onTransform();

    void onDestroy(Lancet lancet);
}
