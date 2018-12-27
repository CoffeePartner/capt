package com.dieyidezui.lancet.rt;

import com.dieyidezui.lancet.rt.internal.annotations.AutoReplaced;

import javax.annotation.Nullable;

public class Lancet {

    private static Lancet instance = new Lancet();

    public static Lancet instance() {
        return instance;
    }

    private Interceptor global;

    /**
     * Add Method Interceptor at runtime, use it carefully!
     */
    public void setGlobalInterceptor(@Nullable Interceptor interceptor) {
        this.global = interceptor;
    }

    @Nullable
    public Interceptor getGlobalInterceptor() {
        return global;
    }

    @AutoReplaced
    public static AroundContext getContext() {
        throw new AssertionError("Don't invoke it outside hook method!");
    }
}
