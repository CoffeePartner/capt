package com.dieyidezui.lancet.plugin.api.annotations;

import java.lang.annotation.*;

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
