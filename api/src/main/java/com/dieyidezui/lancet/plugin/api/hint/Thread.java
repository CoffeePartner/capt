package com.dieyidezui.lancet.plugin.api.hint;

import java.lang.annotation.*;

/**
 * Notify function on which thread type
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Thread {

    Type value();

}
