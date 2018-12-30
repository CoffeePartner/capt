package com.dieyidezui.capt.plugin.process.plugin;

import com.android.build.gradle.BaseExtension;
import com.dieyidezui.capt.plugin.api.Arguments;
import com.dieyidezui.capt.plugin.api.Context;
import com.dieyidezui.capt.plugin.api.CaptInternal;
import com.dieyidezui.capt.plugin.api.OutputProvider;
import com.dieyidezui.capt.plugin.api.graph.ClassGraph;
import org.gradle.api.Project;

import java.net.URLClassLoader;

public class ForwardingCapt implements CaptInternal {

    private final CaptInternal delegate;

    public ForwardingCapt(CaptInternal delegate) {
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
    public URLClassLoader captLoader() {
        return delegate.captLoader();
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
