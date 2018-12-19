package com.dieyidezui.lancet.plugin.process;

import com.android.build.api.transform.TransformInvocation;
import com.dieyidezui.lancet.plugin.api.*;
import com.dieyidezui.lancet.plugin.process.plugin.GlobalLancet;
import com.dieyidezui.lancet.plugin.process.plugin.PluginWrapper;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;
import com.dieyidezui.lancet.plugin.resource.VariantResource;
import com.dieyidezui.lancet.plugin.util.Constants;
import com.google.common.io.Closeables;
import okio.BufferedSource;
import okio.Okio;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PluginManager implements Constants {

    private final VariantResource resource;
    private final TransformInvocation invocation;
    private Map<String, PluginBean> prePlugins = Collections.emptyMap();
    private final List<PluginWrapper> plugins = new ArrayList<>();

    public PluginManager(GlobalResource global, VariantResource resource, TransformInvocation invocation) {
        this.resource = resource;
        this.invocation = invocation;
    }

    public Consumer<List<PluginBean>> asConsumer() {
        return l -> prePlugins = l.stream().collect(Collectors.toMap(PluginBean::getId, Function.identity()));
    }

    public Supplier<List<PluginBean>> asSupplier() {
        return () -> plugins.stream().map(PluginWrapper::toBean).collect(Collectors.toList());
    }

    public void initPlugins(CommonArgs args, GlobalLancet globalLancet) throws IOException {
        for (String id : args.plugins()) {
            Class<? extends Plugin> clazz = findPluginInProperties(id);
            if (clazz == null) {
                clazz = findPluginInApkGraph(id);
            }
            if (clazz == null) {
                throw pluginNotFound(id, null, null);
            }
            try {
                Plugin plugin = clazz.newInstance();

                PluginWrapper wrapper = new PluginWrapper(invocation.isIncremental() && prePlugins.containsKey(id),
                        plugin,
                        args.asArgumentsFor(id),
                        id,
                        resource,
                        globalLancet) {
                };
                plugins.add(wrapper);
            } catch (IllegalAccessException | InstantiationException ex) {
                throw pluginNotFound(id, clazz.getName(), ex);
            }
        }

        // priority order
        plugins.sort(Comparator.comparingInt(l -> l.getArgs().getMyArguments().priority()));

        // call before create
        plugins.forEach(PluginWrapper::callBeforeCreate);
    }

    private Class<? extends Plugin> findPluginInProperties(String id) throws IOException {
        Enumeration<URL> urls = resource.loadPluginOnLancet(id);
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
            return resource.loadClass(className).asSubclass(Plugin.class);
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

}
