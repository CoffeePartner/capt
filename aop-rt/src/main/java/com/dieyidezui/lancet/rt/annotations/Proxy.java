package com.dieyidezui.lancet.rt.annotations;

import com.dieyidezui.lancet.rt.internal.annotations.AutoReplaced;

/**
 * Indicate the hook method.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@AutoReplaced
public @interface Proxy {
    /**
     * The target method name.
     */
    String value();

    /**
     * Priority to sort hook methods, the smaller, the higher.
     */
    int priority() default 0;
}
