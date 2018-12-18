package com.dieyidezui.lancet.plugin.lancetplugin;

import com.dieyidezui.lancet.plugin.api.*;
import com.dieyidezui.lancet.plugin.api.process.MetaProcessor;
import com.dieyidezui.lancet.plugin.api.transform.ClassTransformer;
import com.dieyidezui.lancet.plugin.resource.VariantResource;

import java.lang.annotation.Annotation;

public class PluginScopeLancet extends ForwardingLancet {

    private final String id;
    private final VariantResource resource;


    public PluginScopeLancet(String id, VariantResource resource, LancetInternal delegate) {
        super(delegate);
        this.id = id;
        this.resource = resource;
    }


    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public Arguments getArgs() {
        return null;
    }

    @Override
    public void registerMetaProcessor(MetaProcessor processor, Class<? extends Annotation>... interestedIn) {
    }

    @Override
    public void registerClassTransformer(ClassTransformer transformer) {

    }

    @Override
    public OutputProvider outputs() {
        return resource.provider(id);
    }
}
