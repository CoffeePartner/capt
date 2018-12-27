package com.dieyidezui.lancet.rt.annotations;

import com.dieyidezui.lancet.rt.internal.annotations.AutoReplaced;

/**
 * Indicate the hook method.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@AutoReplaced
public @interface Insert {

    /**
     * The target method name.
     */
    String value();

    /**
     * if true, create empty method which only invoke super if not exits
     */
    boolean mayCreateSuper() default false;

    /**
     * Priority to sort hook methods, the smaller, the higher.
     */
    int priority() default 0;
}
