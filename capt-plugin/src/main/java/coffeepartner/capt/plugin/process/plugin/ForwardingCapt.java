package coffeepartner.capt.plugin.process.plugin;

import com.android.build.gradle.BaseExtension;
import coffeepartner.capt.plugin.api.Arguments;
import coffeepartner.capt.plugin.api.Context;
import coffeepartner.capt.plugin.api.CaptInternal;
import coffeepartner.capt.plugin.api.OutputProvider;
import coffeepartner.capt.plugin.api.graph.ClassGraph;
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
    public ClassGraph getClassGraph() {
        return delegate.getClassGraph();
    }

    @Override
    public Arguments getArgs() {
        return delegate.getArgs();
    }

    @Override
    public OutputProvider getOutputs() {
        return delegate.getOutputs();
    }
}
