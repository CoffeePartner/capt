package com.dieyidezui.lancet.plugin;

import com.dieyidezui.lancet.plugin.api.LancetPlugin;
import com.dieyidezui.lancet.plugin.dsl.LancetPluginExtension;
import com.dieyidezui.lancet.plugin.gradle.GradleLancetExtension;
import com.dieyidezui.lancet.plugin.util.Constants;
import com.google.common.io.Closeables;
import okio.BufferedSource;
import okio.Okio;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class PluginManager implements Constants {

    Map<String, LancetPlugin> plugins = new HashMap<>();
    Set<String> definedInApk = new HashSet<>();
    private LancetLoader loader;

    public void initPlugins(GradleLancetExtension extension) throws IOException {
        for (LancetPluginExtension e : extension.getPlugins()) {
            Class<? extends LancetPlugin> clazz = findPluginInProperties(e.getName());
            if (clazz == null) {
                clazz = findPluginInApkGraph(e.getName());
            }
            if (clazz == null) {
                throw pluginNotFound(e.getName(), null, null);
            }
        }
    }

    private Class<? extends LancetPlugin> findPluginInProperties(String pluginName) throws IOException {
        Enumeration<URL> urls = loader.loadPluginOnLancet(pluginName);
        if (urls.hasMoreElements()) {
            BufferedSource bs = Okio.buffer(Okio.source(urls.nextElement().openStream()));
            Properties properties = new Properties();
            properties.load(bs.inputStream());

            String className = properties.getProperty(PLUGIN_KEY);

            if (urls.hasMoreElements()) {
                throw new IllegalStateException("More than one plugin named: " + pluginName);
            }

            Closeables.close(bs, true);
            return loadPluginClass(pluginName, className);
        }
        return null;
    }

    private Class<? extends LancetPlugin> findPluginInApkGraph(String pluginName) {
        // TODO
        return null;
    }

    private Class<? extends LancetPlugin> loadPluginClass(String pluginName, String className) {
        try {
            return loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw pluginNotFound(pluginName, className, e);
        }
    }

    private IllegalStateException pluginNotFound(String pluginName, @Nullable String className, @Nullable Throwable sup) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("LancetPlugin named '").append(pluginName).append("' ");
        if (className != null) {
            sb.append("with className '").append(className).append("' ");
        }
        sb.append("not found");
        return new IllegalStateException(sb.toString(), sup);
    }
}
