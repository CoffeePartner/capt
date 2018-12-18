package com.dieyidezui.lancet.plugin.lancetplugin;

import com.android.build.gradle.BaseExtension;
import com.dieyidezui.lancet.plugin.api.*;
import com.dieyidezui.lancet.plugin.api.graph.ClassGraph;
import com.dieyidezui.lancet.plugin.api.process.MetaProcessor;
import com.dieyidezui.lancet.plugin.api.transform.ClassTransformer;
import org.gradle.api.Project;

import java.lang.annotation.Annotation;

public class PluginContext implements LancetInternal {

    String pluginId;

    Plugin plugin;

    public PluginContext(String pluginId, Plugin plugin ) {
        this.pluginId = pluginId;
        this.plugin = plugin;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public ClassGraph classGraph() {
        return null;
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
        return null;
    }

    @Override
    public Project getProject() {
        return null;
    }

    @Override
    public BaseExtension getAndroid() {
        return null;
    }

    interface Factory {
        PluginContext newPluginContext(Plugin plugin, String id);
    }
}
