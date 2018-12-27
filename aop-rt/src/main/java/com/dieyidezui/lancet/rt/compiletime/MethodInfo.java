package com.dieyidezui.lancet.rt.compiletime;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MethodInfo {


    private final int access;
    private final String name;
    private final String desc;
    private final String signature;
    private final List<String> exception;

    public MethodInfo(int access, String name, String desc, String signature, String[] exception) {
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.signature = signature;
        this.exception = exception == null ? Collections.<String>emptyList() : Arrays.asList(exception);
    }

    /**
     * @return the method's access flags (see {@link Modifier}). This parameter also indicates if
     * the method is synthetic and/or deprecated.
     */
    public int getAccess() {
        return access;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * @return the method's signature. May be {@literal null} if the method parameters,
     * return type and exceptions do not use generic types.
     */
    @Nullable
    public String getSignature() {
        return signature;
    }

    public List<String> getException() {
        return exception;
    }
}
