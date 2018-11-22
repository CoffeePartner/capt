package com.dieyidezui.lancet.rt.internal.codegen;

import com.dieyidezui.lancet.rt.Interceptor;
import com.dieyidezui.lancet.rt.AroundContext;

import javax.annotation.Nullable;

public abstract class GeneratedInterceptor implements Interceptor {

    private AroundContext context;

    @Override
    public final Object intercept(AroundContext context) {
        this.context = context;
        try {
            return intercept(context.getArgs());
        } finally {
            this.context = null;
        }
    }

    final public AroundContext getContext() {
        return context;
    }

    @Nullable
    protected abstract Object intercept(Object[] args);
}
