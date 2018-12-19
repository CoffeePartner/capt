package com.dieyidezui.lancet.plugin.api;

import com.dieyidezui.lancet.plugin.api.log.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public interface Context {

    String getVariantName();

    Logger getLogger(Class<?> clazz);

    ForkJoinPool computation();

    ExecutorService io();
}
