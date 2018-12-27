package com.dieyidezui.lancet.rt.annotations;

import com.dieyidezui.lancet.rt.internal.annotations.AutoRemovedAfterCompile;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@AutoRemovedAfterCompile
public @interface Restrict {

    boolean runtime() default false;

    Class<? extends Annotation>[] bind();
}
