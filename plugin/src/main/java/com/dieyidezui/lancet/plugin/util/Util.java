package com.dieyidezui.lancet.plugin.util;

import com.android.build.api.transform.TransformException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Util {

    public static <T> T await(Future<T> future) throws IOException, TransformException, InterruptedException {
        try {
            return future.get();
        }catch (ExecutionException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else if(e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new TransformException(e.getCause());
        }
    }
}
