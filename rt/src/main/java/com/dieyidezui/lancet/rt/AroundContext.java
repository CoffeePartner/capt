package com.dieyidezui.lancet.rt;


import javax.annotation.Nullable;

public interface AroundContext {

    /**
     * @return The Arguments of the methods, changes to it will affect {@link AroundContext#proceed()}'s arguments on Lancet.getGlobalInterceptor.
     * But it doesn't works on hook method, because {@link AroundContext#proceed()} will collect your hook method's actual arguments as parameters.
     */
    Object[] getArgs();

    /**
     * @return The context where your target method executed.
     * If static, returns null.
     */
    Object getTarget();

    /**
     * @return The object where your target method called.
     * If static, returns null.
     */
    Object getThis();

    @Nullable
    Object proceed();

    @Nullable
    <T1 extends Throwable> Object proceedThrow1() throws T1;

    @Nullable
    <T1 extends Throwable, T2 extends Throwable> Object proceedThrow2() throws T1, T2;

    @Nullable
    <T1 extends Throwable, T2 extends Throwable, T3 extends Throwable> Object proceedThrow3() throws T1, T2, T3;
}
