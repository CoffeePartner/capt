package com.dieyidezui.lancet.rt.annotations;

import java.lang.annotation.*;

/**
 * Indicate the type annotated will be removed after plugin transformed.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
@RemoveWhenTransform
public @interface RemoveWhenTransform {
}
