package com.dieyidezui.lancet.core;


import java.util.Collection;

public interface ClassRequest {

    Classes request();

    Collection<String> rerack();

    /**
     * Useful when request() == Classes.SPECIFIC
     * @return SPECIFIC classes.
     */
    Collection<String> specific();

    enum Classes {
        ALL,
        CHANGED,
        SPECIFIC
    }
}
