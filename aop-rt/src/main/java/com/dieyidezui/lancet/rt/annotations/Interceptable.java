package com.dieyidezui.lancet.rt.annotations;

import com.dieyidezui.lancet.rt.Lancet;
import com.dieyidezui.lancet.rt.internal.annotations.AutoReplaced;

/**
 * Annotate a method to make it interceptable by {@link Lancet#getGlobalInterceptor()}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@AutoReplaced
public @interface Interceptable {
}