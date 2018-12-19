package com.dieyidezui.lancet.plugin.process.plugin;

import com.android.build.gradle.BaseExtension;
import com.dieyidezui.lancet.plugin.api.Arguments;
import com.dieyidezui.lancet.plugin.api.Context;
import com.dieyidezui.lancet.plugin.api.LancetInternal;
import com.dieyidezui.lancet.plugin.api.OutputProvider;
import com.dieyidezui.lancet.plugin.api.graph.ClassGraph;
import com.dieyidezui.lancet.plugin.api.log.Logger;
import com.dieyidezui.lancet.plugin.api.logger.LoggerFactory;
import com.dieyidezui.lancet.plugin.api.process.MetaProcessor;
import com.dieyidezui.lancet.plugin.api.transform.ClassTransformer;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;
import com.dieyidezui.lancet.plugin.resource.VariantResource;
import org.gradle.api.Project;

import java.lang.annotation.Annotation;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class GlobalLancet implements LancetInternal, Context {

    private final ClassGraph classGraph;
    private final GlobalResource global;
    private final VariantResource variantResource;

    public GlobalLancet(ClassGraph classGraph, GlobalResource global, VariantResource variantResource) {
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
    public URLClassLoader lancetLoader() {
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
    public void registerMetaProcessor(MetaProcessor processor, Set<Class<? extends Annotation>> interestedIn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerClassTransformer(ClassTransformer transformer) {
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
