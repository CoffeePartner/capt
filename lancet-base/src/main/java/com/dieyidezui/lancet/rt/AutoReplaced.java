package com.dieyidezui.lancet.rt;

import java.lang.annotation.*;

/**
 * Indicates that annotations will replace / move / transform their annotated methods' bytecode.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@AutoReplaced
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface AutoReplaced {
}
