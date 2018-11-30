package com.dieyidezui.lancet.plugin.cache;

import com.google.common.io.Files;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DirCache {

    private static Logger LOGGER = LoggerFactory.getLogger(DirCache.class);

    private final boolean cacheUseful;
    private final File mDir;
    private final ExecutorService executor;
    private final Gson gson;
    private final List<Future<?>> futures = new ArrayList<>();

    public DirCache(File dir, ExecutorService executor, Gson gson) {
        this.mDir = dir;
        this.executor = executor;
        this.gson = gson;
        if (!dir.exists()) {
            dir.mkdirs();
        }
        cacheUseful = dir.isDirectory();
    }

    public boolean isCacheUseful() {
        return cacheUseful;
    }

    public <T> void loadAsync(Consumer<T> consumer) {
        futures.add(executor.submit(new SingleReadTask<>(consumer)));
    }

    public <T> void storeAsync(Supplier<T> supplier) {
        futures.add(executor.submit(new SingleWriteTask<>(supplier)));
    }

    public boolean await() {
        boolean ret = true;
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                ret = false;
                LOGGER.error("Task failed " + future, e);
            } catch (ExecutionException e) {
                ret = false;
                LOGGER.error("Task failed " + future, e.getCause());
            }
        }
        futures.clear();
        return ret;
    }

    private static Class<?> getTargetType(Object t) {
        return (Class<?>) ((ParameterizedType) t.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
    }


    class SingleReadTask<T> implements Runnable {

        private final Consumer<T> consumer;

        SingleReadTask(Consumer<T> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void run() {
            Class<?> targetType = getTargetType(consumer);
            String fileName = targetType.getSimpleName() + ".json";
            Reader reader = null;

            try {
                reader = Files.newReader(new File(mDir, fileName), Charset.defaultCharset());
                gson.fromJson(reader, targetType);
            } catch (IOException | RuntimeException e) {
                LOGGER.error("Read failed for: " + fileName, e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }
    }

    class SingleWriteTask<T> implements Runnable {
        private final Supplier<T> supplier;

        SingleWriteTask(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public void run() {
            String fileName = getTargetType(supplier).getSimpleName() + ".json";
            Writer writer = null;
            try {
                writer = Files.newWriter(new File(mDir, fileName), Charset.defaultCharset());
                gson.toJson(supplier.get(), writer);
            } catch (IOException | RuntimeException e) {
                LOGGER.error("Write failed for: " + fileName, e);
            } finally {
                IOUtils.closeQuietly(writer);
            }
        }
    }
}
