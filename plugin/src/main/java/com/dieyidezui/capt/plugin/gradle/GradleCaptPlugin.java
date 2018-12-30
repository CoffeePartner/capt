package com.dieyidezui.capt.plugin.gradle;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.dieyidezui.capt.plugin.api.graph.ClassInfo;
import com.dieyidezui.capt.plugin.dsl.CaptPluginExtension;
import com.dieyidezui.capt.plugin.graph.ClassBean;
import com.dieyidezui.capt.plugin.resource.GlobalResource;
import com.dieyidezui.capt.plugin.util.Constants;
import com.dieyidezui.capt.plugin.util.CaptThreadFactory;
import com.dieyidezui.capt.plugin.variant.VariantManager;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class GradleCaptPlugin implements Plugin<Project>, Constants {

    private static final Logger LOGGER = Logging.getLogger(GradleCaptPlugin.class);

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
            throw new ProjectConfigurationException("Only application or library is supported by capt", null);
        }

        project.getExtensions().create(NAME, GradleCaptExtension.class, project.container(CaptPluginExtension.class));


        VariantManager variantManager = new VariantManager(createGlobalResource(project, baseExtension),
                baseExtension, project);
        // callCreate configurations for separate variant
        variantManager.createConfigurationForVariant();

        CaptTransform captTransform = new CaptTransform(variantManager);
        baseExtension.registerTransform(captTransform);
    }

    private static GlobalResource createGlobalResource(Project project, BaseExtension baseExtension) {

        int core = Runtime.getRuntime().availableProcessors();
        // use 20s instead if 60s to opt memory
        // 3 x core threads at most
        // Use it combine with ForkJoinPool
        ExecutorService io = new ThreadPoolExecutor(0, core * 3,
                20L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new CaptThreadFactory());

        // ForkJoinPool.common() just have core - 1, because it use the waiting thread,
        // But we just wait at IO threads, not computation, so we need core threads.
        ForkJoinPool computation = new ForkJoinPool(core);

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                // optimize for List<ClassBean>, reduce array copy
                .registerTypeAdapter(new TypeToken<List<ClassBean>>() {
                }.getType(), (InstanceCreator) select -> new ArrayList<ClassInfo>(Constants.OPT_SIZE))
                .create();

        File root = new File(project.getBuildDir(), NAME);

        return new GlobalResource(project, root, computation, io, gson, (GradleCaptExtension) project.getExtensions().getByName(NAME), baseExtension);
    }
}
