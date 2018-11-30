package com.dieyidezui.lancet.plugin;

import com.android.build.gradle.BaseExtension;
import com.dieyidezui.lancet.plugin.bean.ClassInfo;
import com.dieyidezui.lancet.plugin.cache.DirJsonCache;
import com.dieyidezui.lancet.plugin.util.LancetThreadFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.reflect.TypeToken;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LancetPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        if (project.getPlugins().findPlugin("com.android.application") == null
                && project.getPlugins().findPlugin("com.android.library") == null) {
            throw new ProjectConfigurationException("Need android application/library plugin to be applied first", null);
        }

        // TODO
        project.getGradle().getTaskGraph().whenReady();

        BaseExtension baseExtension = (BaseExtension) project.getExtensions().getByName("android");
        project.getExtensions().create(LancetTransform.NAME, LancetExtension.class);


        int core = Runtime.getRuntime().availableProcessors();

        ExecutorService lancetExecutor = Executors.newFixedThreadPool(core, new LancetThreadFactory());
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                // optimize for List<ClassInfo>, reduce array copy
                .registerTypeAdapter(new TypeToken<List<ClassInfo>>() {
                }.getType(), (InstanceCreator) type -> new ArrayList<ClassInfo>())
                .create();


        DirJsonCache dirCache = new DirJsonCache(new File(project.getBuildDir(), LancetTransform.NAME),
                lancetExecutor,
                gson);

        LancetTransform lancetTransform = new LancetTransform(dirCache);
        baseExtension.registerTransform(lancetTransform);
    }
}
