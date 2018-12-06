package com.dieyidezui.lancet.rt.annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Restrict {

    boolean runtime() default false;

    Class<? extends Annotation> binded();
}
