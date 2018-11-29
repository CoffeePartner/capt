package com.dieyidezui.lancet.plugin;

import com.android.build.gradle.BaseExtension;
import com.dieyidezui.lancet.plugin.cache.DirCache;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;

import java.io.File;

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

        DirCache dirCache = new DirCache(new File(project.getBuildDir(), LancetTransform.NAME));

        LancetTransform lancetTransform = new LancetTransform(dirCache);
        baseExtension.registerTransform(lancetTransform);
        project.afterEvaluate(lancetTransform);
    }
}
