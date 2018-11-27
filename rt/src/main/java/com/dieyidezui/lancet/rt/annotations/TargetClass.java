package com.dieyidezui.lancet.rt.annotations;

import com.dieyidezui.lancet.rt.Scope;

import java.lang.annotation.*;

/**
 * Limit the target classes.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TargetClass {
    String value();

    Scope scope() default Scope.SELF;
}
