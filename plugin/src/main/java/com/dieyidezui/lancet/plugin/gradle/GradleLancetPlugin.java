package com.dieyidezui.lancet.plugin.gradle;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.dieyidezui.lancet.plugin.api.graph.ClassInfo;
import com.dieyidezui.lancet.plugin.cache.OutputProviderFactory;
import com.dieyidezui.lancet.plugin.cache.RelativeDirectoryProviderFactory;
import com.dieyidezui.lancet.plugin.cache.RelativeDirectoryProviderFactoryImpl;
import com.dieyidezui.lancet.plugin.lancetplugin.PluginManager;
import com.dieyidezui.lancet.plugin.resource.DirJsonCache;
import com.dieyidezui.lancet.plugin.dsl.LancetPluginExtension;
import com.dieyidezui.lancet.plugin.resource.FileManager;
import com.dieyidezui.lancet.plugin.resource.ResourceManager;
import com.dieyidezui.lancet.plugin.util.Constants;
import com.dieyidezui.lancet.plugin.util.LancetThreadFactory;
import com.dieyidezui.lancet.plugin.variant.VariantManager;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import org.gradle.api.*;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GradleLancetPlugin implements Plugin<Project>, Constants {

    private static final Logger LOGGER = Logging.getLogger(GradleLancetPlugin.class);

    @Override
    public void apply(Project project) {
        if (project.getPlugins().findPlugin("com.android.application") == null
                && project.getPlugins().findPlugin("android") == null
                && project.getPlugins().findPlugin("com.android.library") == null
                && project.getPlugins().findPlugin("android-library") == null) {
            throw new ProjectConfigurationException("Need android application or library plugin to be applied first", null);
        }

        BaseExtension baseExtension = (BaseExtension) project.getExtensions().getByName("android");
        if (!(baseExtension instanceof AppExtension || baseExtension instanceof LibraryExtension)) {
            throw new ProjectConfigurationException("Only application or library is supported by lancet", null);
        }

        project.getExtensions().create(NAME, GradleLancetExtension.class, project.container(LancetPluginExtension.class));

        FileManager files = new FileManager(new File(project.getBuildDir(), NAME));

        RelativeDirectoryProviderFactory singleFactory = new RelativeDirectoryProviderFactoryImpl();

        OutputProviderFactory factory = new OutputProviderFactory(singleFactory, files.asSelector());

        int core = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(core, new LancetThreadFactory());

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                // optimize for List<ClassInfo>, reduce array copy
                .registerTypeAdapter(new TypeToken<List<ClassInfo>>() {
                }.getType(), (InstanceCreator) select -> new ArrayList<ClassInfo>(Constants.OPT_SIZE))
                .create();

        ResourceManager resourceManager = new ResourceManager(
                files, factory, executor, gson);

        VariantManager variantManager = new VariantManager(baseExtension, project);
        // create configurations for separate variant
        variantManager.createConfigurationForVariant();


        PluginManager pluginManager = new PluginManager();

        DirJsonCache dirCache = new DirJsonCache(new File(project.getBuildDir(), NAME),
                lancetExecutor,
                gson);

        // ClassGraph classGraph = new ClassGraph();
        LancetTransform lancetTransform = new LancetTransform(resourceManager, variantManager, pluginManager);
        baseExtension.registerTransform(lancetTransform);
    }
}
