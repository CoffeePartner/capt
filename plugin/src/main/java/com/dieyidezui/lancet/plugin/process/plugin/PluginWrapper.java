package com.dieyidezui.lancet.plugin.process.plugin;

import com.dieyidezui.lancet.plugin.api.Arguments;
import com.dieyidezui.lancet.plugin.api.LancetInternal;
import com.dieyidezui.lancet.plugin.api.OutputProvider;
import com.dieyidezui.lancet.plugin.api.Plugin;
import com.dieyidezui.lancet.plugin.resource.VariantResource;

@SuppressWarnings("unchecked")
public abstract class PluginWrapper extends ForwardingLancet {

    private final boolean incremental;
    private final Plugin plugin;
    private final Arguments args;
    private final String id;
    private final VariantResource resource;

    public PluginWrapper(boolean incremental, Plugin plugin,
                         Arguments args,
                         String id, VariantResource resource, LancetInternal delegate) {
        super(delegate);
        this.incremental = incremental;
        this.plugin = plugin;
        this.args = args;
        this.id = id;
        this.resource = resource;
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
}
