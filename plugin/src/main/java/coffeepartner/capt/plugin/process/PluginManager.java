package coffeepartner.capt.plugin.process;

import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import coffeepartner.capt.plugin.api.Plugin;
import coffeepartner.capt.plugin.api.graph.Status;
import coffeepartner.capt.plugin.dsl.CaptPluginExtension;
import coffeepartner.capt.plugin.gradle.GradleCaptExtension;
import coffeepartner.capt.plugin.graph.ApkClassGraph;
import coffeepartner.capt.plugin.graph.ApkClassInfo;
import coffeepartner.capt.plugin.process.plugin.GlobalCapt;
import coffeepartner.capt.plugin.process.plugin.PluginWrapper;
import coffeepartner.capt.plugin.process.visitors.AnnotationClassDispatcher;
import coffeepartner.capt.plugin.process.visitors.ThirdRound;
import coffeepartner.capt.plugin.resource.GlobalResource;
import coffeepartner.capt.plugin.resource.VariantResource;
import coffeepartner.capt.plugin.util.Constants;
import coffeepartner.capt.plugin.util.WaitableTasks;
import com.google.common.io.Closeables;
import okio.BufferedSource;
import okio.Okio;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PluginManager implements Constants {

    private static final Logger LOGGER = Logging.getLogger(PluginManager.class);
    private boolean hasPluginRemoved = false;
    private final GlobalResource global;
    private final VariantResource resource;
    private final TransformInvocation invocation;
    private Map<String, PluginBean> prePlugins = Collections.emptyMap();
    private final Map<String, Plugin> plugins = new ConcurrentHashMap<>();
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

    public boolean initPlugins(GradleCaptExtension extension, int scope, GlobalCapt globalCapt) throws IOException, TransformException, InterruptedException {
        WaitableTasks tasks = WaitableTasks.get(global.io());
        for (CaptPluginExtension e : extension.getPlugins()) {
            tasks.submit(() -> {
                Class<? extends Plugin> clazz = findPluginInProperties(e.getName());
                if (clazz == null) {
                    clazz = findPluginInApkGraph(e.getName());
                }
                if (clazz == null) {
                    LOGGER.warn("Capt plugin with id '{}' not found", e.getName());
                } else {
                    try {
                        plugins.put(e.getName(), clazz.newInstance());
                    } catch (IllegalAccessException | InstantiationException ex) {
                        throw loadClassFailed(e.getName(), clazz.getName(), ex);
                    }
                }
                return null;
            });
        }
        tasks.await();

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
                    globalCapt);
            PluginBean pre = prePlugins.get(entry.getKey());
            if (incremental && pre != null) {
                wrapper.combinePre(pre);
            }
            wrappers.add(wrapper);
        }

        // optimize for AnnotationDispatcher
        hasPluginRemoved = incremental && !plugins.keySet().containsAll(prePlugins.keySet());

        // priority order
        wrappers.sort(Comparator.comparingInt(l -> l.getArgs().getMyArguments().priority()));
        return incremental;
    }

    public ThirdRound.TransformProviderFactory forThird() {
        return new ThirdRound.TransformProviderFactory() {
            @Override
            public Stream<ApkClassInfo> collectRemovedPluginsAffectedClasses(ApkClassGraph graph) {
                return hasPluginRemoved
                        ? prePlugins.entrySet()
                        .stream()
                        .filter(e -> !plugins.containsKey(e.getKey()))
                        .map(Map.Entry::getValue)
                        .flatMap(b -> b.getAffectedClasses().stream())
                        .map(graph::get)
                        .filter(Objects::nonNull)
                        .filter(c -> c.status() == Status.NOT_CHANGED)
                        : Stream.empty();// others are already called.
            }

            @Override
            public Stream<ThirdRound.TransformProvider> create() {
                return wrappers.stream().map(PluginWrapper::newTransformProvider);
            }
        };
    }

    public AnnotationClassDispatcher.AnnotationProcessorFactory forAnnotation() {
        return () -> wrappers.stream()
                .map(PluginWrapper::newAnnotationProvider)
                .filter(Objects::nonNull);
    }

    public Set<String> getAllSupportedAnnotations() {
        return wrappers.stream().flatMap(w -> w.getSupportedAnnotations().stream()).collect(Collectors.toSet());
    }

    private Class<? extends Plugin> findPluginInProperties(String id) throws IOException {
        URL url = resource.loadPluginOnCapt(id);
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
            return resource.loadPluginClass(className).asSubclass(Plugin.class);
        } catch (ClassNotFoundException e) {
            throw loadClassFailed(id, className, e);
        }
    }

    private IllegalStateException loadClassFailed(String id, String className, Throwable cause) {
        String sb = "Capt plugin with id '" + id + "' load class '" + className + "' failed";
        return new IllegalStateException(sb, cause);
    }

    public void callCreate() throws IOException, InterruptedException, TransformException {

        // before callCreate
        wrappers.forEach(PluginWrapper::callBeforeCreate);

        // on callCreate
        WaitableTasks waitable = WaitableTasks.get(global.io());
        wrappers.forEach(p -> waitable.submit(() -> {
            p.callOnCreate();
            return null;
        }));
        waitable.await();
    }

    public void callDestroy() throws IOException, InterruptedException, TransformException {
        WaitableTasks waitable = WaitableTasks.get(global.io());
        wrappers.forEach(p -> waitable.submit(() -> {
            p.callOnDestroy();
            return null;
        }));
        waitable.await();
    }

    public boolean hasPluginRemoved() {
        return hasPluginRemoved;
    }

    public static class LastPlugins {
        public List<PluginBean> plugins;

        public LastPlugins(List<PluginBean> plugins) {
            this.plugins = plugins;
        }
    }
}
