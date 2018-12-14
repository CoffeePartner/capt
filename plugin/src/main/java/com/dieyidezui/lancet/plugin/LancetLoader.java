package com.dieyidezui.lancet.plugin;

import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.TransformInvocation;
import com.dieyidezui.lancet.plugin.util.Constants;
import com.dieyidezui.lancet.plugin.variant.VariantManager;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.stream.Stream;

public class LancetLoader implements Constants {

    private static final Logger LOGGER = Logging.getLogger(LancetLoader.class);

    private URLClassLoader runnerLoader;
    private URLClassLoader runtimeLoader;

    public LancetLoader() {
    }


    public void beforeTransform(TransformInvocation invocation, VariantManager variantManager) {
        String variantName = invocation.getContext().getVariantName();
        Configuration target = variantManager.getByVariant(variantName);
        URLClassLoader lancetDependencies = URLClassLoader.newInstance(
                target.resolve().stream()
                        .map(f -> {
                            try {
                                return f.toURI().toURL();
                            } catch (MalformedURLException e) {
                                throw new AssertionError(e);
                            }
                        }).toArray(URL[]::new), Thread.currentThread().getContextClassLoader());

        URL[] runtimeUrls = invocation.getInputs().stream()
                .flatMap(s -> Stream.concat(s.getDirectoryInputs().stream(), s.getJarInputs().stream()))
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
        //LOGGER.error(Arrays.toString(runtimeUrls));
    }

    public Class<?> loadApkClass(String className) throws ClassNotFoundException {
        return runtimeLoader.loadClass(className);
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return runnerLoader.loadClass(className);
    }

    public Enumeration<URL> loadPluginOnLancet(String pluginName) throws IOException {
        return runnerLoader.getParent().getResources(PLUGIN_PATH + pluginName + ".properties");
    }
}
