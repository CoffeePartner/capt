package com.dieyidezui.capt.plugin.api.transform;


import java.util.Collections;
import java.util.Set;

public abstract class ClassRequest {

    public Scope scope() {
        return Scope.CHANGED;
    }

    /**
     * Regardless of scope(), extra specified classes
     *
     * @return extra specified classes
     */
    public Set<String> extraSpecified() {
        return Collections.emptySet();
    }

    public enum Scope {
        ALL,
        CHANGED,
        NONE,
    }
}
