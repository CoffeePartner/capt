package com.dieyidezui.lancet.rt;

import com.dieyidezui.lancet.rt.compiletime.AnnotationContext;
import com.dieyidezui.lancet.rt.compiletime.CodeApperance;
import com.dieyidezui.lancet.rt.internal.annotations.AutoRemovedAfterCompile;


@AutoRemovedAfterCompile
public interface RestrictProcessor {

    boolean accept(CodeApperance code, AnnotationContext ctx);
}
