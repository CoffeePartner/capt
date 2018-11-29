package com.dieyidezui.lancet.plugin.cache;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.Executor;

public class DirCache {

    private static Logger logger = LoggerFactory.getLogger(DirCache.class);

    private boolean cacheUseful = true;

    private File mDir;
    private Executor executor;

    public DirCache(File dir) {
        this.mDir = dir;
        dir.mkdirs();
        if (!dir.isDirectory()) {
            cacheUseful = false;
        }
    }

    public void afterEvaluate(Executor executor) {
        this.executor = executor;
    }

    public boolean isCacheUseful() {
        return cacheUseful;
    }

    public <T> void loadAsync(String file, Callback<T> callback) {

    }

    public boolean await() {

    }

    public interface Callback<T> {
        void onLoad(T t);
    }
}
