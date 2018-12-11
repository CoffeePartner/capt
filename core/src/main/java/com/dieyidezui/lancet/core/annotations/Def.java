package com.dieyidezui.lancet.core.annotations;

import java.lang.annotation.*;

@Documented
@java.lang.annotation.Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Def {

    /**
     * @return name of your plugin.
     */
    String value();

    /**
     * Priority decides the order to process classes.
     * The less, the higher.
     * <p/>
     * It can be override by configure the lancet extension, like:
     * <p/>
     * lancet {
     * plugins {
     * your_plugin_name {
     * priority 10
     * }
     * }
     * }
     *
     * @return priority of your plugin, default 0.
     */
    int priority() default 0;

    Class<? extends Annotation>[] interested();
}
