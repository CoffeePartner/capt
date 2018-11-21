package com.dieyidezui.lancet.rt.internal;


import com.dieyidezui.lancet.rt.AroundContext;
import com.dieyidezui.lancet.rt.Interceptor;

import javax.annotation.Nullable;
import java.util.List;

public class AroundMethodChain implements AroundContext {

    private final Object target;
    private final Object thiz;
    private final int index;
    private final List<Interceptor> interceptors;
    private final Object[] args;

    public AroundMethodChain(Object target, Object thiz, int index, List<Interceptor> interceptors, Object[] args) {
        this.target = target;
        this.thiz = thiz;
        this.index = index;
        this.interceptors = interceptors;
        this.args = args;
    }

    @Nullable
    @Override
    public Object getTarget() {
        return target;
    }

    @Nullable
    @Override
    public Object getThis() {
        return thiz;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    @Nullable
    @Override
    public <T1 extends Throwable> Object proceedThrow1() throws T1 {
        return proceed();
    }

    @Nullable
    @Override
    public <T1 extends Throwable, T2 extends Throwable> Object proceedThrow2() throws T1, T2 {
        return proceed();
    }

    @Nullable
    @Override
    public <T1 extends Throwable, T2 extends Throwable, T3 extends Throwable> Object proceedThrow3() throws T1, T2, T3 {
        return proceed();
    }

    @Nullable
    @Override
    public Object proceed() {
        return proceed(args.clone());
    }

    /**
     * public for generated code!
     *
     * @param args don't clone args
     */
    @Nullable
    public Object proceed(Object[] args) {
        if (this.index > interceptors.size()) {
            throw new AssertionError();
        }
        AroundMethodChain next = new AroundMethodChain(target, thiz, index + 1, interceptors, args);
        return interceptors.get(index).intercept(next);
    }
}
