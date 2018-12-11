package com.dieyidezui.lancet.core.annotations;

import java.lang.annotation.*;

/**
 * Indicate the type annotated will be removed after plugin transformed.
 * If the annotated type is annotation, recursively removed also.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
@AutoRemoved
public @interface AutoRemoved {
    String value() default "";
}
