package com.dieyidezui.lancet.plugin.cache;

import com.android.build.api.transform.TransformException;
import com.dieyidezui.lancet.plugin.api.util.RelativeDirectoryProvider;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;
import com.dieyidezui.lancet.plugin.util.Constants;
import com.google.common.io.Closeables;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class InternalCache {

    private static Logger LOGGER = LoggerFactory.getLogger(InternalCache.class);

    private final List<Future<?>> futures = new ArrayList<>();
    private final GlobalResource global;
    private final RelativeDirectoryProvider provider;

    public InternalCache(RelativeDirectoryProvider provider, GlobalResource global) {
        this.provider = provider;
        this.global = global;
    }

    public <T> void loadAsync(Consumer<T> consumer) {
        futures.add(global.io().submit(new SingleReadTask<>(consumer)));
    }

    public <T> void storeAsync(Supplier<T> supplier) {
        futures.add(global.io().submit(new SingleWriteTask<>(supplier)));
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

    @SuppressWarnings("unchecked")
    private static <T> Class<T> getTargetType(Object t) {
        return (Class<T>) ((ParameterizedType) t.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
    }

    class SingleReadTask<T> implements Callable<Void> {

        private final Consumer<T> consumer;

        SingleReadTask(Consumer<T> consumer) {
            this.consumer = consumer;
        }

        @Override
        public Void call() throws Exception {
            Class<T> targetType = getTargetType(consumer);
            String fileName = targetType.getSimpleName() + ".json";
            BufferedSource bs = null;

            try {
                bs = provider.asSource(fileName);
                consumer.accept(global.gson().fromJson(new InputStreamReader(bs.inputStream(), Constants.UTF8), targetType));
            } catch (IOException | RuntimeException e) {
                LOGGER.error("Read failed for {}", fileName);
                throw e;
            } finally {
                Closeables.close(bs, true);
            }
            return null;
        }
    }

    class SingleWriteTask<T> implements Callable<Void> {
        private final Supplier<T> supplier;

        SingleWriteTask(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public Void call() throws Exception {
            String fileName = getTargetType(supplier).getSimpleName() + ".json";
            BufferedSink bs = null;
            try {
                bs = provider.asSink(fileName);
                OutputStreamWriter os = new OutputStreamWriter(bs.outputStream(), Constants.UTF8);
                global.gson().toJson(supplier.get(), os);
                os.flush();
            } catch (IOException | RuntimeException e) {
                LOGGER.error("Write failed for {}" + fileName);
                throw e;
            } finally {
                Closeables.close(bs, true);
            }
            return null;
        }
    }
}
