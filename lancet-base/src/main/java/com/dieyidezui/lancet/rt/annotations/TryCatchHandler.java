package com.dieyidezui.lancet.rt.annotations;

import com.dieyidezui.lancet.rt.AutoReplaced;

import java.lang.annotation.*;

/**
 * Pre process the classes who extends {@link Throwable}.
 * The method desc should like (A)A (A is a class that extends Throwable).
 * Combine with {@link NameRegex} to restrict the scope.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@AutoReplaced
public @interface TryCatchHandler {
}
