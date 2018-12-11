package com.dieyidezui.lancet.plugin;

import com.android.build.gradle.AppExtension;
import com.dieyidezui.lancet.plugin.cache.DirJsonCache;
import com.dieyidezui.lancet.plugin.dsl.LancetPluginExtension;
import com.dieyidezui.lancet.plugin.util.LancetThreadFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LancetPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        if (project.getPlugins().findPlugin("com.android.application") == null &&
                project.getPlugins().findPlugin("android") == null) {
            throw new ProjectConfigurationException("Need com.android.application / android plugin to be applied first", null);
        }

        AppExtension appExtension = (AppExtension) project.getExtensions().getByName("android");


        project.getExtensions().create(LancetTransform.NAME, LancetExtension.class, project.container(LancetPluginExtension.class));

        ClassLoaderMaker maker = new ClassLoaderMaker(appExtension, project);
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
        appExtension.registerTransform(lancetTransform);
    }
}
