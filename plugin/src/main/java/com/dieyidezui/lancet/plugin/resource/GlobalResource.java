package com.dieyidezui.lancet.plugin.resource;

import com.google.gson.Gson;

import java.io.File;
import java.util.concurrent.ExecutorService;

public class GlobalResource {

    private final File root;
    private final ExecutorService service;
    private final Gson gson;

    public GlobalResource(File root, ExecutorService service, Gson gson) {
        this.root = root;
        this.service = service;
        this.gson = gson;
    }

    public ExecutorService service() {
        return service;
    }

    public Gson gson() {
        return gson;
    }

    public File root() {
        return root;
    }
}
