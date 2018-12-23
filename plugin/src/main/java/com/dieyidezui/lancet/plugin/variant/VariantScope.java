package com.dieyidezui.lancet.plugin.variant;

import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.api.BaseVariant;
import com.dieyidezui.lancet.plugin.cache.InternalCache;
import com.dieyidezui.lancet.plugin.cache.OutputProviderFactory;
import com.dieyidezui.lancet.plugin.cache.RelativeDirectoryProviderFactory;
import com.dieyidezui.lancet.plugin.cache.RelativeDirectoryProviderFactoryImpl;
import com.dieyidezui.lancet.plugin.dsl.LancetPluginExtension;
import com.dieyidezui.lancet.plugin.graph.ApkClassGraph;
import com.dieyidezui.lancet.plugin.process.PluginManager;
import com.dieyidezui.lancet.plugin.process.dispatch.ClassDispatcher;
import com.dieyidezui.lancet.plugin.process.plugin.GlobalLancet;
import com.dieyidezui.lancet.plugin.cache.FileManager;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;
import com.dieyidezui.lancet.plugin.resource.VariantResource;
import com.dieyidezui.lancet.plugin.util.Constants;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.annotation.Nullable;
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
        RelativeDirectoryProviderFactory singleFactory = new RelativeDirectoryProviderFactoryImpl();
        OutputProviderFactory factory = new OutputProviderFactory(singleFactory, files.asSelector());
        VariantResource variantResource = new VariantResource(getVariant(),
                files, factory);
        InternalCache internalCache = new InternalCache(singleFactory.newProvider(new File(files.variantRoot(), "core"))
                , global);
        ApkClassGraph graph = new ApkClassGraph(variantResource, global.gradleLancetExtension().getThrowIfDuplicated());
        GlobalLancet lancet = new GlobalLancet(graph, global, variantResource);

        ClassDispatcher dispatcher = new ClassDispatcher(invocation, global);
        PluginManager manager = new PluginManager(global, variantResource, invocation);

        if (invocation.isIncremental()) {
            internalCache.loadAsync(graph.readClasses());
            internalCache.loadAsync(manager.asConsumer());

            internalCache.await();
        }

        int scope = variant.endsWith(ANDROID_TEST) ? LancetPluginExtension.ANDROID_TEST : LancetPluginExtension.ASSEMBLE;
        boolean incremental = manager.initPlugins(global.gradleLancetExtension(), scope, lancet);

        variantResource.init(incremental, invocation, getLancetConfiguration());


        // everything ready, start plugin logic
        manager.callCreate();


        internalCache.storeAsync(manager.asSupplier());
        internalCache.storeAsync(graph.writeClasses());
        internalCache.await();
    }

    public interface Factory {

        VariantScope create(BaseVariant v);

        VariantScope create(BaseVariant v, @Nullable VariantScope parent);
    }
}
