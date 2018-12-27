package com.dieyidezui.lancet.plugin.process;

import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.dieyidezui.lancet.plugin.api.*;
import com.dieyidezui.lancet.plugin.api.graph.Status;
import com.dieyidezui.lancet.plugin.graph.ApkClassGraph;
import com.dieyidezui.lancet.plugin.graph.ApkClassInfo;
import com.dieyidezui.lancet.plugin.dsl.LancetPluginExtension;
import com.dieyidezui.lancet.plugin.gradle.GradleLancetExtension;
import com.dieyidezui.lancet.plugin.process.plugin.GlobalLancet;
import com.dieyidezui.lancet.plugin.process.plugin.PluginWrapper;
import com.dieyidezui.lancet.plugin.process.visitors.ThirdRound;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;
import com.dieyidezui.lancet.plugin.resource.VariantResource;
import com.dieyidezui.lancet.plugin.util.Constants;
import com.dieyidezui.lancet.plugin.util.WaitableTasks;
import com.google.common.io.Closeables;
import okio.BufferedSource;
import okio.Okio;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PluginManager implements Constants {

    private static final Logger LOGGER = Logging.getLogger(PluginManager.class);
    private final GlobalResource global;
    private final VariantResource resource;
    private final TransformInvocation invocation;
    private Map<String, PluginBean> prePlugins = Collections.emptyMap();
    private final Map<String, Plugin> plugins = new HashMap<>();
    private final List<PluginWrapper> wrappers = new ArrayList<>();

    public PluginManager(GlobalResource global, VariantResource resource, TransformInvocation invocation) {
        this.global = global;
        this.resource = resource;
        this.invocation = invocation;
    }

    @SuppressWarnings("Convert2Lambda")
    public Consumer<LastPlugins> readPrePlugins() {
        return new Consumer<LastPlugins>() {
            @Override
            public void accept(LastPlugins l) {
                prePlugins = l.plugins.stream().collect(Collectors.toMap(PluginBean::getId, Function.identity()));
            }
        };
    }

    @SuppressWarnings("Convert2Lambda")
    public Supplier<LastPlugins> writePlugins() {
        return new Supplier<LastPlugins>() {
            @Override
            public LastPlugins get() {
                return new LastPlugins(wrappers.stream().map(PluginWrapper::toBean).collect(Collectors.toList()));
            }
        };
    }

    public boolean initPlugins(GradleLancetExtension extension, int scope, GlobalLancet globalLancet) throws IOException {
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

        boolean incremental = invocation.isIncremental() && prePlugins.keySet().containsAll(plugins.keySet());
        if (incremental != invocation.isIncremental()) {
            LOGGER.lifecycle("Found new plugin applied, turn into full mode");
        }

        CommonArgs args = CommonArgs.createBy(extension, scope, plugins);

        for (Map.Entry<String, Plugin> entry : plugins.entrySet()) {
            PluginWrapper wrapper = new PluginWrapper(
                    incremental,
                    entry.getValue(),
                    args.asArgumentsFor(entry.getKey()),
                    entry.getKey(),
                    resource,
                    globalLancet);
            wrappers.add(wrapper);
        }

        // priority order
        wrappers.sort(Comparator.comparingInt(l -> l.getArgs().getMyArguments().priority()));
        return incremental;
    }

    /**
     * Rerack classes for removed plugin
     */
    public Stream<ApkClassInfo> collectRemovedPluginsAffectedClasses(ApkClassGraph graph) {
        return prePlugins.entrySet().parallelStream()
                .filter(e -> !plugins.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(b -> b.getAffectedClasses().stream())
                .map(graph::get)
                .filter(Objects::nonNull)
                .filter(c -> c.status() == Status.NOT_CHANGED);// others are already called.
    }

    public Stream<ThirdRound.PluginProvider> getProviders() {
        return wrappers.stream().map(PluginWrapper::newProvider).filter(Objects::nonNull);
    }


    private Class<? extends Plugin> findPluginInProperties(String id) throws IOException {
        URL url = resource.loadPluginOnLancet(id);
        if (url != null) {
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            BufferedSource bs = Okio.buffer(Okio.source(connection.getInputStream()));
            Properties properties = new Properties();
            properties.load(bs.inputStream());

            String className = properties.getProperty(PLUGIN_KEY);

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

    public void callCreate() throws IOException, InterruptedException, TransformException {

        // before callCreate
        wrappers.forEach(PluginWrapper::callBeforeCreate);


        // on callCreate
        WaitableTasks waitable = WaitableTasks.get(global.io());
        wrappers.forEach(p -> waitable.execute(p::callOnCreate));
        waitable.await();
    }

    public void callDestroy() throws IOException, InterruptedException, TransformException {
        WaitableTasks waitable = WaitableTasks.get(global.io());
        wrappers.forEach(p -> waitable.execute(p::callOnDestroy));
        waitable.await();
    }

    public static class LastPlugins {
        public List<PluginBean> plugins;

        public LastPlugins(List<PluginBean> plugins) {
            this.plugins = plugins;
        }
    }
}
