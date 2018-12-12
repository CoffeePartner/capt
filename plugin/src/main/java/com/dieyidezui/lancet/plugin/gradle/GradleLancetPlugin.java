package com.dieyidezui.lancet.plugin.gradle;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.dieyidezui.lancet.plugin.LancetLoader;
import com.dieyidezui.lancet.plugin.cache.DirJsonCache;
import com.dieyidezui.lancet.plugin.dsl.LancetPluginExtension;
import com.dieyidezui.lancet.plugin.util.LancetThreadFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.gradle.BuildListener;
import org.gradle.BuildResult;
import org.gradle.api.*;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.execution.TaskExecutionGraphListener;
import org.gradle.api.execution.TaskExecutionListener;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskState;
import org.xml.sax.helpers.XMLFilterImpl;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class GradleLancetPlugin implements Plugin<Project> {

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

        project.getExtensions().create(LancetTransform.NAME, GradleLancetExtension.class, project.container(LancetPluginExtension.class));

        LancetLoader maker = new LancetLoader(baseExtension, project);
        // create configurations for separate variant
        maker.createConfigurationForVariant();

        int core = Runtime.getRuntime().availableProcessors();

        ExecutorService lancetExecutor = Executors.newFixedThreadPool(core, new LancetThreadFactory());
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                // optimize for List<ClassInfo>, reduce array copy
                //.registerTypeAdapter(new TypeToken<List<ClassInfo>>() {
                //}.getType(), (InstanceCreator) type -> new ArrayList<ClassInfo>(Constants.OPT_SIZE))
                .create();


        DirJsonCache dirCache = new DirJsonCache(new File(project.getBuildDir(), LancetTransform.NAME),
                lancetExecutor,
                gson);

        // ClassGraph classGraph = new ClassGraph();
        LancetTransform lancetTransform = new LancetTransform(maker);
        baseExtension.registerTransform(lancetTransform);
        project.getGradle().getTaskGraph().addTaskExecutionListener(new TaskExecutionListener() {
            @Override
            public void beforeExecute(Task task) {
                if(task.getName().startsWith("createFullJar")) {
                    LOGGER.error(task.toString());
                    task.getDependsOn().forEach(o -> {
                        LOGGER.error("d: " + o.toString());
                    });
                }
            }

            @Override
            public void afterExecute(Task task, TaskState state) {

            }
        });
    }
}
