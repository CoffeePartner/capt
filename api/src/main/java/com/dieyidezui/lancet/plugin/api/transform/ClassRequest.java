package com.dieyidezui.lancet.plugin.api.transform;


import java.util.Collection;
import java.util.Collections;

public abstract class ClassRequest {

    public Scope scope() {
        return Scope.CHANGED;
    }

    /**
     * Regardless of scope(), extra specified classes
     * @return extra specified classes
     */
    public Collection<String> extraSpecified() {
        return Collections.emptyList();
    }

    enum Scope {
        ALL,
        CHANGED,
        NONE,
    }
}
