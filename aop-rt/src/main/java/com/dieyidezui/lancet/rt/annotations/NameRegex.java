package com.dieyidezui.lancet.rt.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restrict {@link Proxy} and {@link TryCatchHandler}'s scope, only classes which matched the regex will add the hook code.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NameRegex {
    /**
     * The regex to match the class name, use the internal name, dot to slash
     */
    String value();
}
