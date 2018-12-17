package com.dieyidezui.lancet.plugin.api;

import com.dieyidezui.lancet.plugin.api.process.MetaProcessor;
import com.dieyidezui.lancet.plugin.api.transform.ClassTransformer;

import java.lang.annotation.Annotation;

public abstract class AbstractPluginContext implements Lancet {


    private final String id;
    private final boolean incremental;
    private final Context context;
    private final Arguments args;
    private final OutputProvider provider;

    protected AbstractPluginContext(String id, boolean incremental, Context context, Arguments args, OutputProvider provider) {
        this.id = id;
        this.incremental = incremental;
        this.context = context;
        this.args = args;
        this.provider = provider;
    }

    @Override
    public boolean isIncremental() {
        return incremental;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public Arguments getArgs() {
        return args;
    }

    @Override
    public void registerMetaProcessor(MetaProcessor processor, Class<? extends Annotation>... interestedIn) {
        registerMetaProcessor(id, processor, interestedIn);
    }

    protected abstract void registerMetaProcessor(String id, MetaProcessor processor, Class<? extends Annotation>[] interestedIn);

    @Override
    public void registerClassTransformer(ClassTransformer transformer) {
        registerClassTransformer(id, transformer);
    }

    protected abstract void registerClassTransformer(String id, ClassTransformer transformer);

    @Override
    public OutputProvider outputs() {
        return provider;
    }
}
