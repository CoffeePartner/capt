package com.dieyidezui.lancet.plugin.process.plugin;

import com.dieyidezui.lancet.plugin.api.Arguments;
import com.dieyidezui.lancet.plugin.api.LancetInternal;
import com.dieyidezui.lancet.plugin.api.OutputProvider;
import com.dieyidezui.lancet.plugin.api.Plugin;
import com.dieyidezui.lancet.plugin.api.transform.ClassTransformer;
import com.dieyidezui.lancet.plugin.process.PluginBean;
import com.dieyidezui.lancet.plugin.process.visitors.ThirdRound;
import com.dieyidezui.lancet.plugin.resource.VariantResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public class PluginWrapper extends ForwardingLancet {

    private final boolean incremental;
    private final Plugin plugin;
    private final Arguments args;
    private final String id;
    private final VariantResource resource;
    private final List<String> affected = Collections.synchronizedList(new ArrayList<>());

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

    public Set<String> getSupportedAnnotations() {
        return plugin.getSupportedAnnotations();
    }

    public ThirdRound.PluginProvider newProvider() {
        ClassTransformer transformer = plugin.onTransformClass();
        if (transformer != null) {
            return new ThirdRound.PluginProvider() {


                @Override
                public void onClassAffected(String className) {
                    affected.add(className);
                }

                @Override
                public ClassTransformer transformer() {
                    return transformer;
                }
            };
        }
        return null;
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
        return new PluginBean(id, affected);
    }
}
