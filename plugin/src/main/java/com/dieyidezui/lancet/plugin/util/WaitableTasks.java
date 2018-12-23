package com.dieyidezui.lancet.plugin.util;

import com.android.build.api.transform.TransformException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class WaitableTasks {

    private final ExecutorService executor;
    private final List<Future<?>> futures = new ArrayList<>();

    public static WaitableTasks get(ExecutorService executor) {
        return new WaitableTasks(executor);
    }

    public WaitableTasks(ExecutorService executor) {
        this.executor = executor;
    }

    public void execute(Runnable runnable) {
        futures.add(executor.submit(runnable));
    }

    public <T> Future<T> submit(Callable<T> callable) {
        Future<T> future = executor.submit(callable);
        futures.add(future);
        return future;
    }

    public void await() throws IOException, InterruptedException, TransformException {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                }
                throw new TransformException(e.getCause());
            }
        }
        futures.clear();
    }
}
