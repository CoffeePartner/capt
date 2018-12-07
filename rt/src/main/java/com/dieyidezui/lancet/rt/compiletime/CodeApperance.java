package com.dieyidezui.lancet.rt.compiletime;


import com.dieyidezui.lancet.rt.internal.annotations.AutoRemovedAfterCompile;

@AutoRemovedAfterCompile
public final class CodeApperance {

    private final ClassInfo classInfo;
    private MethodInfo methodInfo;

    public CodeApperance(ClassInfo classInfo) {
        this.classInfo = classInfo;
    }

    public ClassInfo currentClass() {
        return classInfo;
    }

    public MethodInfo currentMethod() {
        return methodInfo;
    }

    public void setMethodInfo(MethodInfo info) {
        this.methodInfo = info;
    }
}
