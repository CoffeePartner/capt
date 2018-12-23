package com.dieyidezui.lancet.plugin.process.plugin;

import com.dieyidezui.lancet.plugin.api.Arguments;
import com.dieyidezui.lancet.plugin.api.LancetInternal;
import com.dieyidezui.lancet.plugin.api.OutputProvider;
import com.dieyidezui.lancet.plugin.api.Plugin;
import com.dieyidezui.lancet.plugin.api.process.MetaProcessor;
import com.dieyidezui.lancet.plugin.api.transform.ClassTransformer;
import com.dieyidezui.lancet.plugin.process.PluginBean;
import com.dieyidezui.lancet.plugin.resource.VariantResource;
import com.dieyidezui.lancet.plugin.util.Functions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class PluginWrapper extends ForwardingLancet {

    private final boolean incremental;
    private final Plugin plugin;
    private final Arguments args;
    private final String id;
    private final VariantResource resource;
    private Supplier<MetaProcessor> processor;
    private Supplier<ClassTransformer> transformer;

    public PluginWrapper(boolean incremental, Plugin plugin,
                         Arguments args,
                         String id, VariantResource resource, LancetInternal delegate) {
        super(delegate);
        this.incremental = incremental;
        this.plugin = plugin;
        this.args = args;
        this.id = id;
        this.resource = resource;

        this.processor = Functions.cache(plugin::onProcessAnnotations);
        this.transformer = Functions.cache(plugin::onTransformClass);
    }

    public String id() {
        return id;
    }

    public void callBeforeCreate() {
        plugin.beforeCreate(this);
    }

    public void callOnCreate() {
        plugin.onCreate(this);
    }

    public void callOnDestroy() {
        plugin.onDestroy(this);
    }

    public Set<String> getSupportedAnnotations() {
        return plugin.getSupportedAnnotations();
    }

    public MetaProcessor getProcessor() {
        return
    }

    @Nullable
    public ClassTransformer getClassTransformer() {
        return plugin.onTransformClass();
    }

    @Override
    public boolean isIncremental() {
        return incremental;
    }

    @Override
    public Arguments getArgs() {
        return args;
    }

    @Override
    public OutputProvider outputs() {
        return resource.provider(id);
    }

    public PluginBean toBean() {
        return new PluginBean(id,);
    }
}
