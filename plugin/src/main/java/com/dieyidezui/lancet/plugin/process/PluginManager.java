package com.dieyidezui.lancet.plugin.process;

import com.android.build.api.transform.TransformInvocation;
import com.dieyidezui.lancet.plugin.api.*;
import com.dieyidezui.lancet.plugin.dsl.LancetPluginExtension;
import com.dieyidezui.lancet.plugin.gradle.GradleLancetExtension;
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
    private final Map<String, Plugin> plugins = new HashMap<>();
    private final List<PluginWrapper> wrappers = new ArrayList<>();

    public PluginManager(GlobalResource global, VariantResource resource, TransformInvocation invocation) {
        this.resource = resource;
        this.invocation = invocation;
    }

    public Consumer<List<PluginBean>> asConsumer() {
        return l -> prePlugins = l.stream().collect(Collectors.toMap(PluginBean::getId, Function.identity()));
    }

    public Supplier<List<PluginBean>> asSupplier() {
        return () -> wrappers.stream().map(PluginWrapper::toBean).collect(Collectors.toList());
    }

    public void initPlugins(GradleLancetExtension extension, int scope, GlobalLancet globalLancet) throws IOException {
        for (LancetPluginExtension e : extension.getPlugins()) {
            Class<? extends Plugin> clazz = findPluginInProperties(e.getName());
            if (clazz == null) {
                clazz = findPluginInApkGraph(e.getName());
            }
            if (clazz == null) {
                throw pluginNotFound(e.getName(), null, null);
            }
            try {
                plugins.put(e.getName(), clazz.newInstance());
            } catch (IllegalAccessException | InstantiationException ex) {
                throw pluginNotFound(e.getName(), clazz.getName(), ex);
            }
        }

        CommonArgs args = CommonArgs.createBy(extension, scope, plugins);

        for (Map.Entry<String, Plugin> entry : plugins.entrySet()) {
            PluginWrapper wrapper = new PluginWrapper(invocation.isIncremental() && prePlugins.containsKey(entry.getKey()),
                    entry.getValue(),
                    args.asArgumentsFor(entry.getKey()),
                    entry.getKey(),
                    resource,
                    globalLancet);
            wrappers.add(wrapper);
        }

        // priority order
        wrappers.sort(Comparator.comparingInt(l -> l.getArgs().getMyArguments().priority()));

        // call before create
        wrappers.forEach(PluginWrapper::callBeforeCreate);
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
