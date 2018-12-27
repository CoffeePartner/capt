package com.dieyidezui.lancet.plugin.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Def {

    /**
     * Default priority, can be override by config.
     *
     * @return default priority
     */
    int defaultPriority() default 0;

    /**
     * @return supported Annotation types
     */
    String[] supportedAnnotationTypes() default {};
}
