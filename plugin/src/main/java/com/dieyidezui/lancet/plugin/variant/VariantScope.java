package com.dieyidezui.lancet.plugin.variant;

import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.api.BaseVariant;
import com.dieyidezui.lancet.plugin.cache.*;
import com.dieyidezui.lancet.plugin.dsl.LancetPluginExtension;
import com.dieyidezui.lancet.plugin.graph.ApkClassGraph;
import com.dieyidezui.lancet.plugin.process.PluginManager;
import com.dieyidezui.lancet.plugin.process.plugin.GlobalLancet;
import com.dieyidezui.lancet.plugin.process.visitors.AnnotationClassDispatcher;
import com.dieyidezui.lancet.plugin.process.visitors.FirstRound;
import com.dieyidezui.lancet.plugin.process.visitors.ThirdRound;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;
import com.dieyidezui.lancet.plugin.resource.VariantResource;
import com.dieyidezui.lancet.plugin.util.ClassWalker;
import com.dieyidezui.lancet.plugin.util.Constants;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;


public class VariantScope implements Constants {
    private static Logger LOGGER = Logging.getLogger(VariantScope.class);

    private final String variant;
    private Configuration lancetConfiguration;
    private final GlobalResource global;
    private final FileManager files;

    VariantScope(String variant, Configuration lancetConfiguration, GlobalResource global) {
        this.variant = variant;
        this.lancetConfiguration = lancetConfiguration;
        this.global = global;
        this.files = new FileManager(new File(global.root(), getVariant()));
    }

    public Configuration getLancetConfiguration() {
        return lancetConfiguration;
    }

    public File getRoot() {
        return files.variantRoot();
    }

    public String getVariant() {
        return variant;
    }

    public void doTransform(TransformInvocation invocation) throws IOException, TransformException, InterruptedException {

        // load and prepare
        ClassWalker walker = new ClassWalker(global, invocation);
        AnnotationClassDispatcher annotationClassDispatcher = new AnnotationClassDispatcher(global);


        RelativeDirectoryProviderFactory singleFactory = new RelativeDirectoryProviderFactoryImpl();
        OutputProviderFactory factory = new OutputProviderFactory(singleFactory, files.asSelector());
        VariantResource variantResource = new VariantResource(getVariant(),
                files, factory);
        variantResource.init(invocation, getLancetConfiguration());
        InternalCache internalCache = new InternalCache(singleFactory.newProvider(new File(files.variantRoot(), "core"))
                , global);
        ApkClassGraph graph = new ApkClassGraph(variantResource, global.gradleLancetExtension().getThrowIfDuplicated());
        GlobalLancet lancet = new GlobalLancet(graph, global, variantResource);

        PluginManager manager = new PluginManager(global, variantResource, invocation);

        if (invocation.isIncremental()) {
            internalCache.loadAsync(graph.readClasses());
            internalCache.loadAsync(manager.readPrePlugins());
            internalCache.loadAsync(annotationClassDispatcher.readPreMatched());
            internalCache.await();
        }

        int scope = variant.endsWith(ANDROID_TEST) ? LancetPluginExtension.ANDROID_TEST : LancetPluginExtension.ASSEMBLE;
        boolean incremental = manager.initPlugins(global.gradleLancetExtension(), scope, lancet);
        variantResource.setIncremental(incremental);

        // Round 1: make class graph & collect metas
        // use the invocation.isIncremental()
        new FirstRound(graph)
                .accept(walker,
                        invocation.isIncremental(),
                        annotationClassDispatcher.toCollector(manager.getAllSupportedAnnotations()));
        graph.markRemovedClassesAndBuildGraph();

        // everything ready, call plugin lifecycle
        manager.callCreate();

        // Round 2: visit Metas
        annotationClassDispatcher.dispatchMetas(
                incremental,
                graph,
                variantResource,
                manager.forAnnotation());

        // Round 3: transform classes
        // use the actual incremental (for plugins input)
        // remember to ignore removed classes if incremental
        new ThirdRound(global, graph)
                .accept(incremental,
                        walker,
                        manager.forThird(),
                        invocation);

        // transform done, store cache
        internalCache.storeAsync(graph.writeClasses());
        internalCache.storeAsync(manager.writePlugins());
        internalCache.storeAsync(annotationClassDispatcher.writeMatched());

        manager.callDestroy(); // call destroy after store to save time
        internalCache.await();
    }

    public interface Factory {

        VariantScope create(BaseVariant v);

        VariantScope create(BaseVariant v, VariantScope parent);
    }
}
