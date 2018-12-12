package com.dieyidezui.lancet.core;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Map;

public interface PluginContext {

    boolean incremental();

    Map<String, Object> getArgs();

    File getOutputClassDir();

    File getCacheDir();
}
