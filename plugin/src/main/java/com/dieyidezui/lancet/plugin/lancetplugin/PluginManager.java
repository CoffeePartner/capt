package com.dieyidezui.lancet.plugin.lancetplugin;

import com.dieyidezui.lancet.plugin.api.*;
import com.dieyidezui.lancet.plugin.api.process.MetaProcessor;
import com.dieyidezui.lancet.plugin.api.transform.ClassTransformer;
import com.dieyidezui.lancet.plugin.dsl.LancetPluginExtension;
import com.dieyidezui.lancet.plugin.resource.VariantResource;
import com.dieyidezui.lancet.plugin.util.Constants;
import com.google.common.io.Closeables;
import okio.BufferedSource;
import okio.Okio;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;

public class PluginManager implements Constants {

    Map<String, Plugin> plugins = new HashMap<>();
    Set<String> definedInApk = new HashSet<>();

    public PluginManager() {
    }

    public void findPlugins(VariantResource variantResource) throws IOException {
        for (LancetPluginExtension e : extension.getPlugins()) {
            Class<? extends Plugin> clazz = findPluginInProperties(e.getName());
            if (clazz == null) {
                clazz = findPluginInApkGraph(e.getName());
            }
            if (clazz == null) {
                throw pluginNotFound(e.getName(), null, null);
            }

            try {
                Plugin plugin = clazz.newInstance();
            } catch (IllegalAccessException | InstantiationException ex) {
                throw pluginNotFound(e.getName(), clazz.getName(), ex);
            }
        }
    }

    public void buildLancetForEachPlugin()

    private Class<? extends Plugin> findPluginInProperties(String id) throws IOException {
        Enumeration<URL> urls = loader.loadPluginOnLancet(id);
        if (urls.hasMoreElements()) {
            BufferedSource bs = Okio.buffer(Okio.source(urls.nextElement().openStream()));
            Properties properties = new Properties();
            properties.load(bs.inputStream());

            String className = properties.getProperty(PLUGIN_KEY);

            if (urls.hasMoreElements()) {
                throw new IllegalStateException("More than one plugin with id '" + id + "'");
            }

            Closeables.close(bs, true);
            return loadPluginClass(id, className);
        }
        return null;
    }

    private Class<? extends Plugin> findPluginInApkGraph(String pluginName) {
        // doesn't support yet
        return null;
    }

    private Class<? extends Plugin> loadPluginClass(String id, String className) {
        try {
            return loader.loadClass(className).asSubclass(Plugin.class);
        } catch (ClassNotFoundException e) {
            throw pluginNotFound(id, className, e);
        }
    }

    private IllegalStateException pluginNotFound(String id, @Nullable String className, @Nullable Throwable sup) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Lancet plugin with id '").append(id).append("' ");
        if (className != null) {
            sb.append("with className '").append(className).append("' ");
        }
        sb.append("not found");
        return new IllegalStateException(sb.toString(), sup);
    }


    private static class PluginWrapper implements Lancet {
        private final String id;
        private final Plugin plugin;

        public PluginWrapper(String id, Plugin plugin) {
            this.id = id;
            this.plugin = plugin;
        }

        @Override
        public boolean isIncremental() {
            return false;
        }

        @Override
        public Context getContext() {
            return null;
        }

        @Override
        public Arguments getArgs() {
            return null;
        }

        @Override
        public void registerMetaProcessor(MetaProcessor processor, Class<? extends Annotation>... interestedIn) {

        }

        @Override
        public void registerClassTransformer(ClassTransformer transformer) {

        }

        @Override
        public OutputProvider outputs() {
            return null;
        }
    }
}
