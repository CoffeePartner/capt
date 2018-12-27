package com.dieyidezui.lancet.rt.annotations;

/**
 * Annotates a parameter that its actual desc is the value().
 * The parameter should be instance of the desc of class.
 * For example:
 * void foo(@ClassOf("java.util.HashMap") HashMap map); // useless for @ClassOf
 * void foo(@ClassOf("java.util.HashMap") Map map); // work
 * void foo(@ClassOf("java.util.HashMap[]") Object obj); // work
 * void foo(@ClassOf("java.util.HashMap[]") Map[] maps); // work
 * void foo(@ClassOf("java.util.HashMap[]") Map map); // doesn't work
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ClassOf {
    String value();
}
