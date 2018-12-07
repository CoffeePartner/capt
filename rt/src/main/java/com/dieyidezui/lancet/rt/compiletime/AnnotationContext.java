package com.dieyidezui.lancet.rt.compiletime;

import com.dieyidezui.lancet.rt.internal.annotations.AutoRemovedAfterCompile;

import java.util.Map;

@AutoRemovedAfterCompile
public class AnnotationContext {

    private Object holder;
    private final String annotationName;
    private final Map<String, Object> values;

    public AnnotationContext(String annotationName, Map<String, Object> values) {
        this.annotationName = annotationName;
        this.values = values;
    }

    public String getAnnotationName() {
        return annotationName;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public Object getHolder() {
        return holder;
    }

    public void setHolder(Object holder) {
        this.holder = holder;
    }
}
