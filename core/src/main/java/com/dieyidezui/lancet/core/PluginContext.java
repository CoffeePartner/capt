package com.dieyidezui.lancet.core;

import javax.annotation.processing.Processor;
import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Map;

public interface PluginContext {

    void registerAnnotationProsessor(Class<? extends Annotation> targets, Processor processor);

    void registerClassProcessorFactory();

    boolean incremental();

    Map<String, Object> getArgs();

    File getOutputClassDir();

    File getCacheDir();
}
