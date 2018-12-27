package com.dieyidezui.lancet.rt.internal.codegen;

import com.dieyidezui.lancet.rt.AroundContext;
import com.dieyidezui.lancet.rt.Interceptor;
import com.dieyidezui.lancet.rt.Lancet;
import com.dieyidezui.lancet.rt.annotations.Interceptable;
import com.dieyidezui.lancet.rt.annotations.TryCatchHandler;
import com.dieyidezui.lancet.rt.internal.AroundMethodChain;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodegenHelper {

    /**
     * For {@link Interceptable}
     */
    private static GeneratedInterceptor[] EMPTY = new GeneratedInterceptor[0];

    /**
     * Invoked by generated code at hook point.
     *
     * @param interceptorArray sorted by priority
     */
    public static void doAround(@Nullable Object target, @Nullable Object thiz, Object[] args, GeneratedInterceptor[] interceptorArray) {

        List<Interceptor> interceptors = new ArrayList<>(interceptorArray.length + 1);

        Interceptor global = Lancet.instance().getGlobalInterceptor();
        if (global != null) {
            interceptors.add(global);
        }
        interceptors.addAll(Arrays.asList(interceptorArray));
        // Optimize for first invoke, reduce clone args once
        new AroundMethodChain(target, thiz, 0, interceptors, null).proceed(args);
    }

    /**
     * For {@link TryCatchHandler}
     */
    public static Object onThrow(@Nullable Object target, Object[] args, GeneratedInterceptor[] interceptors) {
        return new AroundMethodChain(target, null, 0, Arrays.<Interceptor>asList(interceptors), null).proceed(args);
    }

    /**
     * Lancet.getContext() will redirect to this method
     */
    public static AroundContext getContext(GeneratedInterceptor interceptor) {
        return interceptor.getContext();
    }

    public static GeneratedInterceptor[] emptyArray() {
        return EMPTY;
    }
}
