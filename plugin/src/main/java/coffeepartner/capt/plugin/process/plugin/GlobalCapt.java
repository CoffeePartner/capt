package coffeepartner.capt.plugin.process.plugin;

import com.android.build.gradle.BaseExtension;
import coffeepartner.capt.plugin.api.Arguments;
import coffeepartner.capt.plugin.api.Context;
import coffeepartner.capt.plugin.api.CaptInternal;
import coffeepartner.capt.plugin.api.OutputProvider;
import coffeepartner.capt.plugin.api.graph.ClassGraph;
import coffeepartner.capt.plugin.api.log.Logger;
import coffeepartner.capt.plugin.api.logger.LoggerFactory;
import coffeepartner.capt.plugin.resource.GlobalResource;
import coffeepartner.capt.plugin.resource.VariantResource;
import org.gradle.api.Project;

import java.net.URLClassLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class GlobalCapt implements CaptInternal, Context {

    private final ClassGraph classGraph;
    private final GlobalResource global;
    private final VariantResource variantResource;

    public GlobalCapt(ClassGraph classGraph, GlobalResource global, VariantResource variantResource) {
        this.classGraph = classGraph;
        this.global = global;
        this.variantResource = variantResource;
    }

    @Override
    public Project getProject() {
        return global.project();
    }

    @Override
    public BaseExtension getAndroid() {
        return global.android();
    }

    @Override
    public URLClassLoader captLoader() {
        return variantResource.loader();
    }

    @Override
    public boolean isIncremental() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public ClassGraph classGraph() {
        return classGraph;
    }

    @Override
    public Arguments getArgs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public OutputProvider outputs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVariantName() {
        return variantResource.variant();
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    @Override
    public ForkJoinPool computation() {
        return global.computation();
    }

    @Override
    public ExecutorService io() {
        return global.io();
    }
}
