package com.dieyidezui.capt.plugin.variant;

import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.api.BaseVariant;
import com.dieyidezui.capt.plugin.cache.*;
import com.dieyidezui.capt.plugin.dsl.CaptPluginExtension;
import com.dieyidezui.capt.plugin.graph.ApkClassGraph;
import com.dieyidezui.capt.plugin.process.PluginManager;
import com.dieyidezui.capt.plugin.process.plugin.GlobalCapt;
import com.dieyidezui.capt.plugin.process.visitors.AnnotationClassDispatcher;
import com.dieyidezui.capt.plugin.process.visitors.FirstRound;
import com.dieyidezui.capt.plugin.process.visitors.ThirdRound;
import com.dieyidezui.capt.plugin.resource.GlobalResource;
import com.dieyidezui.capt.plugin.resource.VariantResource;
import com.dieyidezui.capt.plugin.util.ClassWalker;
import com.dieyidezui.capt.plugin.util.Constants;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;


public class VariantScope implements Constants {
    private static Logger LOGGER = Logging.getLogger(VariantScope.class);

    private final String variant;
    private Configuration captConfiguration;
    private final GlobalResource global;
    private final FileManager files;

    VariantScope(String variant, Configuration captConfiguration, GlobalResource global) {
        this.variant = variant;
        this.captConfiguration = captConfiguration;
        this.global = global;
        this.files = new FileManager(new File(global.root(), getVariant()));
    }

    public Configuration getCaptConfiguration() {
        return captConfiguration;
    }

    public File getRoot() {
        return files.variantRoot();
    }

    public String getVariant() {
        return variant;
    }

    public void doTransform(TransformInvocation invocation) throws IOException, TransformException, InterruptedException {

        long pre = System.currentTimeMillis();
        long cur;
        // load and prepare
        ClassWalker walker = new ClassWalker(global, invocation);
        AnnotationClassDispatcher annotationClassDispatcher = new AnnotationClassDispatcher(global);


        RelativeDirectoryProviderFactory singleFactory = new RelativeDirectoryProviderFactoryImpl();
        OutputProviderFactory factory = new OutputProviderFactory(singleFactory, files.asSelector());
        VariantResource variantResource = new VariantResource(getVariant(),
                files, factory);
        variantResource.init(invocation, global.android().getBootClasspath(), getCaptConfiguration());
        InternalCache internalCache = new InternalCache(singleFactory.newProvider(new File(files.variantRoot(), "core"))
                , global);
        ApkClassGraph graph = new ApkClassGraph(variantResource, global.gradleCaptExtension().getThrowIfDuplicated());
        GlobalCapt capt = new GlobalCapt(graph, global, variantResource);

        PluginManager manager = new PluginManager(global, variantResource, invocation);

        if (invocation.isIncremental()) {
            internalCache.loadSync(manager.readPrePlugins());
        }

        int scope = variant.endsWith(ANDROID_TEST) ? CaptPluginExtension.ANDROID_TEST : CaptPluginExtension.ASSEMBLE;
        boolean incremental = manager.initPlugins(global.gradleCaptExtension(), scope, capt);
        variantResource.setIncremental(incremental);

        if (incremental) {
            internalCache.loadAsync(graph.readClasses());
            internalCache.loadAsync(annotationClassDispatcher.readPreMatched());
            internalCache.await();
        }

        cur = System.currentTimeMillis();
        LOGGER.lifecycle("Prepare, cost: {}ms", (cur - pre));
        pre = cur;

        // Round 1: make class graph & collect metas
        new FirstRound(graph)
                .accept(walker,
                        incremental,
                        annotationClassDispatcher.toCollector(manager.getAllSupportedAnnotations()));
        graph.markRemovedClassesAndBuildGraph();

        cur = System.currentTimeMillis();
        LOGGER.lifecycle("Build class graph, cost: {}ms", (cur - pre));
        pre = cur;

        // everything ready, call plugin lifecycle
        manager.callCreate();

        cur = System.currentTimeMillis();
        LOGGER.lifecycle("Call plugins create, cost: {}ms", (cur - pre));
        pre = cur;

        // Round 2: visit Metas
        annotationClassDispatcher.dispatch(
                manager.hasPluginRemoved(),
                incremental,
                graph,
                variantResource,
                manager.forAnnotation());

        cur = System.currentTimeMillis();
        LOGGER.lifecycle("Dispatch annotations, cost: {}ms", (cur - pre));
        pre = cur;

        // Round 3: transform classes
        new ThirdRound(variantResource, global, graph)
                .accept(incremental,
                        walker,
                        manager.forThird(),
                        invocation);

        cur = System.currentTimeMillis();
        LOGGER.lifecycle("Transform classes, cost: {}ms", (cur - pre));
        pre = cur;
        // transform done, store cache
        internalCache.storeAsync(graph.writeClasses());
        internalCache.storeAsync(manager.writePlugins());
        internalCache.storeAsync(annotationClassDispatcher.writeMatched());

        manager.callDestroy(); // call destroy after store to save time
        internalCache.await();

        cur = System.currentTimeMillis();
        LOGGER.lifecycle("Store cache and call plugins destroy, cost: {}ms", (cur - pre));
    }

    public interface Factory {

        VariantScope create(BaseVariant v);

        VariantScope create(BaseVariant v, VariantScope parent);
    }
}
