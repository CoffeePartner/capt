package com.dieyidezui.lancet.rt.annotations;

import com.dieyidezui.lancet.rt.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Limit the target classes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ImplementedInterface {

    /**
     * Interface array, java type name, $ for inner class.
     * For example : a.b.c$d;
     */
    String[] value();

    /**
     * The scope of interface array.
     */
    Scope scope() default Scope.SELF;
}
