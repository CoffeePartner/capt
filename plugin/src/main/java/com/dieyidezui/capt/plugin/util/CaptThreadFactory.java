package com.dieyidezui.capt.plugin.util;


import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CaptThreadFactory implements ThreadFactory {
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public CaptThreadFactory() {
        namePrefix = "capt-io-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r,
                namePrefix + threadNumber.getAndIncrement());
        thread.setPriority(Thread.NORM_PRIORITY - 1); // io lower than normal

        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        return thread;
    }
}
