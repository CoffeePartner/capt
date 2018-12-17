package com.dieyidezui.lancet.plugin.resource;

import com.dieyidezui.lancet.plugin.gradle.GradleLancetExtension;
import com.google.gson.Gson;

import java.io.File;
import java.util.concurrent.ExecutorService;

public class GlobalResource {

    private final File root;
    private final ExecutorService executor;
    private final Gson gson;

    private final GradleLancetExtension gradleLancetExtension;

    public GlobalResource(File root, ExecutorService executor, Gson gson, GradleLancetExtension gradleLancetExtension) {
        this.root = root;
        this.executor = executor;
        this.gson = gson;
        this.gradleLancetExtension = gradleLancetExtension;
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
}
