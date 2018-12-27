package com.dieyidezui.lancet.rt.internal.annotations;


import java.lang.annotation.*;

/**
 * Indicate the type annotated will be removed after plugin transformed.
 * If the annotated type is annotation, recursively removed also.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@AutoRemovedAfterCompile
public @interface AutoRemovedAfterCompile {
}
