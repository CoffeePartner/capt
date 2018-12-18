package com.dieyidezui.lancet.plugin.gradle;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.dieyidezui.lancet.plugin.api.graph.ClassInfo;
import com.dieyidezui.lancet.plugin.dsl.LancetPluginExtension;
import com.dieyidezui.lancet.plugin.graph.ApkClassInfo;
import com.dieyidezui.lancet.plugin.graph.ClassBean;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;
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


        VariantManager variantManager = new VariantManager(createGlobalResource(project, baseExtension),
                baseExtension, project);
        // create configurations for separate variant
        variantManager.createConfigurationForVariant();

        LancetTransform lancetTransform = new LancetTransform(variantManager);
        baseExtension.registerTransform(lancetTransform);
    }

    private static GlobalResource createGlobalResource(Project project, BaseExtension baseExtension) {
        int core = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(core, new LancetThreadFactory());

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                // optimize for List<ClassBean>, reduce array copy
                .registerTypeAdapter(new TypeToken<List<ClassBean>>() {
                }.getType(), (InstanceCreator) select -> new ArrayList<ClassInfo>(Constants.OPT_SIZE))
                .create();

        File root = new File(project.getBuildDir(), NAME);

        return new GlobalResource(project, root, executor, gson, (GradleLancetExtension) project.getExtensions().getByName(NAME), baseExtension);
    }
}
