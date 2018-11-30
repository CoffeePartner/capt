package com.dieyidezui.lancet.plugin.util;


import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class LancetThreadFactory implements ThreadFactory {
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public LancetThreadFactory() {
        namePrefix = "lancet-pool-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r,
                namePrefix + threadNumber.getAndIncrement());
    }
}
