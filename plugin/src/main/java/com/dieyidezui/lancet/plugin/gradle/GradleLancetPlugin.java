package com.dieyidezui.lancet.plugin.gradle;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.internal.pipeline.TransformTask;
import com.dieyidezui.lancet.plugin.LancetLoader;
import com.dieyidezui.lancet.plugin.cache.DirJsonCache;
import com.dieyidezui.lancet.plugin.dsl.LancetPluginExtension;
import com.dieyidezui.lancet.plugin.util.Constants;
import com.dieyidezui.lancet.plugin.util.LancetThreadFactory;
import com.dieyidezui.lancet.plugin.variant.VariantManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.gradle.api.*;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GradleLancetPlugin implements Plugin<Project> , Constants {

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


        VariantManager variantManager = new VariantManager(baseExtension, project);
        LancetLoader maker = new LancetLoader();
        // create configurations for separate variant
        variantManager.createConfigurationForVariant();

        int core = Runtime.getRuntime().availableProcessors();

        ExecutorService lancetExecutor = Executors.newFixedThreadPool(core, new LancetThreadFactory());
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                // optimize for List<ClassInfo>, reduce array copy
                //.registerTypeAdapter(new TypeToken<List<ClassInfo>>() {
                //}.getType(), (InstanceCreator) type -> new ArrayList<ClassInfo>(Constants.OPT_SIZE))
                .create();


        DirJsonCache dirCache = new DirJsonCache(new File(project.getBuildDir(), NAME),
                lancetExecutor,
                gson);

        // ClassGraph classGraph = new ClassGraph();
        LancetTransform lancetTransform = new LancetTransform(maker, variantManager);
        baseExtension.registerTransform(lancetTransform);

        // The fucking transform API doesn't provide evaluating time variant!
        project.afterEvaluate(p -> p.getTasks()
                .withType(TransformTask.class, t -> {
                    if (t.getTransform().getName().equals(NAME)) {
                        t.dependsOn(variantManager.getByVariant(t.getVariantName()));
                    }
                }));
    }
}
