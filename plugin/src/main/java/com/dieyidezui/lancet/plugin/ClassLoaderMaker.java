package com.dieyidezui.lancet.plugin;

import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.AppExtension;
import com.android.builder.model.SourceProvider;
import com.dieyidezui.lancet.plugin.util.Constants;
import com.google.common.collect.Streams;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.SourceSet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;

public class ClassLoaderMaker implements Constants {

    static final Logger LOGGER = Logging.getLogger(ClassLoaderMaker.class);

    private final AppExtension extension;
    private final Project project;
    private URLClassLoader runnerLoader;
    private URLClassLoader runtimeLoader;

    public ClassLoaderMaker(AppExtension extension, Project project) {
        this.extension = extension;
        this.project = project;
    }


    public void beforeTransform(TransformInvocation invocation) {
        String variantName = invocation.getContext().getVariantName();

        ConfigurationContainer configurations = project.getConfigurations();

        Configuration target = configurations.getByName(computeConfigurationName(variantName));

        target.extendsFrom(extension.getApplicationVariants()
                .stream()
                .filter(variant -> variant.getName().equals(variantName))
                .flatMap(variant -> variant.getSourceSets().stream())
                .map(SourceProvider::getName)
                .map(ClassLoaderMaker::computeConfigurationName)
                .map(configurations::getByName)
                .filter(c -> c != target)
                .toArray(Configuration[]::new));

        URLClassLoader lancetDependencies = URLClassLoader.newInstance(
                target.getFiles().stream()
                        .map(f -> {
                            try {
                                return f.toURI().toURL();
                            } catch (MalformedURLException e) {
                                throw new AssertionError(e);
                            }
                        })
                        .toArray(URL[]::new), Thread.currentThread().getContextClassLoader());

        URL[] runtimeUrls = invocation.getInputs().stream()
                .flatMap(s -> Streams.concat(s.getDirectoryInputs().stream(), s.getJarInputs().stream()))
                .map(QualifiedContent::getFile)
                .map(f -> {
                    try {
                        return f.toURI().toURL();
                    } catch (MalformedURLException e) {
                        throw new AssertionError(e);
                    }
                })
                .toArray(URL[]::new);

        this.runnerLoader = URLClassLoader.newInstance(runtimeUrls, lancetDependencies);
        this.runtimeLoader = URLClassLoader.newInstance(runtimeUrls, null);

        LOGGER.error(Arrays.toString(lancetDependencies.getURLs()));
        LOGGER.error(Arrays.toString(runtimeUrls));
    }

    public void createConfigurationForVariant() {
        extension.getSourceSets().all(androidSourceSet -> project.getConfigurations().create(computeConfigurationName(androidSourceSet.getName())));
    }

    public Class<?> loadApkClass(String className) throws ClassNotFoundException {
        return runtimeLoader.loadClass(className);
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return runnerLoader.loadClass(className);
    }

    public Enumeration<URL> getLancetPluginProperties() throws IOException {
        return runnerLoader.getParent().getResources(META);
    }

    private static String computeConfigurationName(String name) {
        if (SourceSet.MAIN_SOURCE_SET_NAME.equals(name)) {
            return NAME;
        }
        return name + CAPITALIZED_NAME;
    }
}
