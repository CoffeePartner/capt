package com.dieyidezui.lancet.plugin.api;

import com.dieyidezui.lancet.plugin.api.annotations.Meta;

/**
 * Lifecycle:
 * 1. Apply every plugin in priority order
 * 2. postApply plugin concurrently
 * 3. Parse classes with {@link Meta} by {@link MetaProcessor}
 * 4. Transform every class from inputs by {@link ClassTransformer}
 */
public interface Plugin {

    /**
     * Apply the plugin, don't do too much work(such as read / write file) in this function.
     *
     * @param lancet
     */
    void apply(Lancet lancet);

    /**
     * Do your time-consuming in this function
     */
    void postApply(Lancet lancet);
}
