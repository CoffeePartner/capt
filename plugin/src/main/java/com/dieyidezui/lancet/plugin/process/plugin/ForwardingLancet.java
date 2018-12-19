package com.dieyidezui.lancet.plugin.process.plugin;

import com.android.build.gradle.BaseExtension;
import com.dieyidezui.lancet.plugin.api.Arguments;
import com.dieyidezui.lancet.plugin.api.Context;
import com.dieyidezui.lancet.plugin.api.LancetInternal;
import com.dieyidezui.lancet.plugin.api.OutputProvider;
import com.dieyidezui.lancet.plugin.api.graph.ClassGraph;
import com.dieyidezui.lancet.plugin.api.process.MetaProcessor;
import com.dieyidezui.lancet.plugin.api.transform.ClassTransformer;
import org.gradle.api.Project;

import java.lang.annotation.Annotation;
import java.net.URLClassLoader;
import java.util.Set;

public class ForwardingLancet implements LancetInternal {

    private final LancetInternal delegate;

    public ForwardingLancet(LancetInternal delegate) {
        this.delegate = delegate;
    }

    @Override
    public Project getProject() {
        return delegate.getProject();
    }

    @Override
    public BaseExtension getAndroid() {
        return delegate.getAndroid();
    }

    @Override
    public URLClassLoader lancetLoader() {
        return delegate.lancetLoader();
    }

    @Override
    public boolean isIncremental() {
        return delegate.isIncremental();
    }

    @Override
    public Context getContext() {
        return delegate.getContext();
    }

    @Override
    public ClassGraph classGraph() {
        return delegate.classGraph();
    }

    @Override
    public Arguments getArgs() {
        return delegate.getArgs();
    }

    @Override
    public OutputProvider outputs() {
        return delegate.outputs();
    }
}
