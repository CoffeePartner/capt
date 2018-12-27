package com.dieyidezui.lancet.rt.annotations;


import java.lang.annotation.*;

/**
 * Only class with @Meta will be dispatch to plugins.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Meta {
}
