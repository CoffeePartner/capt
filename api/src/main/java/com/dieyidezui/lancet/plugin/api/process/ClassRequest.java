package com.dieyidezui.lancet.plugin.api.process;


import java.util.Collection;
import java.util.Collections;

public abstract class ClassRequest {

    public Classes request() {
        return Classes.CHANGED;
    }

    /**
     * Useful when request() == Classes.SPECIFIC
     *
     * @return SPECIFIC classes.
     */
    public Collection<String> specific() {
        return Collections.emptyList();
    }

    public abstract void onIncrementalToFull();

    /**
     * Request rerack classes
     *
     * @return
     */
    public Collection<String> rerack() {
        return Collections.emptyList();
    }

    enum Classes {
        ALL,
        CHANGED,
        SPECIFIC,
        NONE,
    }
}
