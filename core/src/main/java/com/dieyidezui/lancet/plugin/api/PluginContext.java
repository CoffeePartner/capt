package com.dieyidezui.lancet.plugin.api;

import java.io.File;
import java.util.Map;

public interface PluginContext {

    boolean incremental();

    Map<String, Object> getArgs();

    File getOutputClassDir();

    File getCacheDir();
}
