package com.dieyidezui.lancet.plugin.resource;

import com.android.build.gradle.BaseExtension;
import com.dieyidezui.lancet.plugin.gradle.GradleLancetExtension;
import com.google.gson.Gson;
import org.gradle.api.Project;

import java.io.File;
import java.util.concurrent.ExecutorService;

public class GlobalResource {

    private final Project project;
    private final File root;
    private final ExecutorService executor;
    private final Gson gson;

    private final GradleLancetExtension gradleLancetExtension;
    private final BaseExtension extension;

    public GlobalResource(Project project, File root, ExecutorService executor, Gson gson, GradleLancetExtension gradleLancetExtension, BaseExtension extension) {
        this.project = project;
        this.root = root;
        this.executor = executor;
        this.gson = gson;
        this.gradleLancetExtension = gradleLancetExtension;
        this.extension = extension;
    }

    public Project project() {
        return project;
    }

    public ExecutorService executor() {
        return executor;
    }

    public Gson gson() {
        return gson;
    }

    public File root() {
        return root;
    }

    public GradleLancetExtension gradleLancetExtension() {
        return gradleLancetExtension;
    }

    public BaseExtension android() {
        return extension;
    }
}
