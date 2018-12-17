package com.dieyidezui.lancet.plugin.resource;

import com.android.build.api.transform.TransformInvocation;
import com.dieyidezui.lancet.plugin.api.OutputProvider;
import com.dieyidezui.lancet.plugin.cache.OutputProviderFactory;
import com.dieyidezui.lancet.plugin.util.Constants;
import com.google.gson.Gson;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;

public class ResourceManager implements Constants {

    private static final Logger LOGGER = Logging.getLogger(Loader.class);

    private final Loader loader = new Loader();
    private final FileManager files;
    private final OutputProviderFactory factory;
    private final ExecutorService executor;
    private final Gson gson;
    private TransformInvocation invocation;

    public ResourceManager(FileManager files, OutputProviderFactory factory, ExecutorService executor, Gson gson) {
        this.files = files;
        this.factory = factory;
        this.executor = executor;
        this.gson = gson;
    }


    public File getLancetRoot() {
        return files.lancetRoot();
    }

    public GlobalResource global() {
        return global;
    }

    public void prepare(TransformInvocation invocation, Configuration target) throws IOException {
        this.invocation = invocation;
        this.loader.initClassLoader(target);
        this.files.attachContext(invocation);
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return loader.loadClass(className);
    }

    public Enumeration<URL> loadPluginOnLancet(String pluginName) throws IOException {
        return loader.loadPluginOnLancet(pluginName);
    }

    public OutputProvider provider(String id) {
        return factory.newProvider(id);
    }

    static class Loader {

        private URLClassLoader runnerLoader;
        //private URLClassLoader runtimeLoader;

        void initClassLoader(Configuration target) {
            //String variantName = invocation.getContext().getVariantName();
            URLClassLoader lancetDependencies = URLClassLoader.newInstance(
                    target.resolve().stream()
                            .map(f -> {
                                try {
                                    return f.toURI().toURL();
                                } catch (MalformedURLException e) {
                                    throw new AssertionError(e);
                                }
                            }).toArray(URL[]::new), Thread.currentThread().getContextClassLoader());

//            URL[] runtimeUrls = invocation.getInputs().stream()
//                    .flatMap(s -> Stream.concat(s.getDirectoryInputs().stream(), s.getJarInputs().stream()))
//                    .map(QualifiedContent::getFile)
//                    .map(f -> {
//                        try {
//                            return f.toURI().toURL();
//                        } catch (MalformedURLException e) {
//                            throw new AssertionError(e);
//                        }
//                    })
//                    .toArray(URL[]::new);

            this.runnerLoader = lancetDependencies;
            //this.runtimeLoader = URLClassLoader.newInstance(runtimeUrls, null);

            LOGGER.error(Arrays.toString(lancetDependencies.getURLs()));
            //LOGGER.error(Arrays.toString(runtimeUrls));
        }

//        public Class<?> loadApkClass(String className) throws ClassNotFoundException {
//            return runtimeLoader.loadClass(className);
//        }

        public Class<?> loadClass(String className) throws ClassNotFoundException {
            return runnerLoader.loadClass(className);
        }

        public Enumeration<URL> loadPluginOnLancet(String pluginName) throws IOException {
            return runnerLoader.getParent().getResources(PLUGIN_PATH + pluginName + ".properties");
        }
    }

}
