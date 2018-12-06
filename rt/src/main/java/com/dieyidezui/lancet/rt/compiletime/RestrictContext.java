package com.dieyidezui.lancet.rt.compiletime;

import java.lang.annotation.Annotation;

public final class RestrictContext<T extends Annotation, U> {

    private U holder;
    private final T annotation;
    private final CodeApperance code;

    public RestrictContext(T annotation, CodeApperance code) {
        this.annotation = annotation;
        this.code = code;
    }

    public U annotationToBean(AnnotationMapper<T, U> mapper) {
        if (holder != null) {
            return holder;
        } else {
            return holder = mapper.apply(annotation);
        }
    }

    @Override
    public String toString() {
        return "RestrictContext{" +
                "holder=" + holder +
                ", annotation=" + annotation +
                '}';
    }


    public interface AnnotationMapper<T extends Annotation, U> {
        U apply(T t);
    }
}
