package com.dieyidezui.lancet.plugin;

import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.TestVariant;
import com.android.build.gradle.internal.api.TestedVariant;
import com.android.builder.model.SourceProvider;
import com.dieyidezui.lancet.plugin.util.Constants;
import org.gradle.api.DomainObjectCollection;
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
import java.util.Enumeration;
import java.util.stream.Stream;

public class LancetLoader implements Constants {

    private static final Logger LOGGER = Logging.getLogger(LancetLoader.class);

    private final BaseExtension extension;
    private final Project project;
    private URLClassLoader runnerLoader;
    private URLClassLoader runtimeLoader;

    public LancetLoader(BaseExtension extension, Project project) {
        this.extension = extension;
        this.project = project;
    }


    public void beforeTransform(TransformInvocation invocation) {
        String variantName = invocation.getContext().getVariantName();
        String sourceSetName = androidTestVariantToSourceSetName(variantName);
        ConfigurationContainer configurations = project.getConfigurations();


        Configuration target = configurations.getByName(computeConfigurationName(sourceSetName));

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

        //LOGGER.error(Arrays.toString(lancetDependencies.getURLs()));
        //LOGGER.error(Arrays.toString(runtimeUrls));
    }

    public boolean isApplication() {
        return extension instanceof AppExtension;
    }

    public void createConfigurationForVariant() {
        ConfigurationContainer configurations = project.getConfigurations();
        extension.getSourceSets().all(androidSourceSet -> {
            if (!androidSourceSet.getName().startsWith(TEST)) { // don't support test
                configurations.maybeCreate(computeConfigurationName(androidSourceSet.getName()));
            }
        });

        DomainObjectCollection<?> collection = extension instanceof AppExtension ?
                ((AppExtension) extension).getApplicationVariants()
                : ((LibraryExtension) extension).getLibraryVariants();

        collection.all(v -> {
            BaseVariant base = (BaseVariant) v;
            Configuration lancetVariant = configurations.getByName(computeConfigurationName(base.getName()));
            base.getSourceSets().stream()
                    .map(SourceProvider::getName)
                    .map(LancetLoader::computeConfigurationName)
                    .map(configurations::getByName)
                    .filter(c -> c != lancetVariant)
                    .forEach(lancetVariant::extendsFrom);

            TestVariant t = ((TestedVariant) v).getTestVariant();
            if (t != null) {
                Configuration testLancetVariant = configurations.getByName(computeConfigurationName(androidTestVariantToSourceSetName(t.getName())));

                testLancetVariant.extendsFrom(lancetVariant);

                t.getSourceSets().stream()
                        .map(SourceProvider::getName)
                        .map(LancetLoader::computeConfigurationName)
                        .map(configurations::getByName)
                        .filter(c -> c != testLancetVariant)
                        .forEach(testLancetVariant::extendsFrom);
            }
        });
    }

    public Class<?> loadApkClass(String className) throws ClassNotFoundException {
        return runtimeLoader.loadClass(className);
    }

    public <T> Class<? extends T> loadClass(String className) throws ClassNotFoundException {
        return (Class<? extends T>) runnerLoader.loadClass(className);
    }

    public Enumeration<URL> loadPluginOnLancet(String pluginName) throws IOException {
        return runnerLoader.getParent().getResources(PLUGIN_PATH + pluginName + ".properties");
    }

    private static String computeConfigurationName(String name) {
        if (SourceSet.MAIN_SOURCE_SET_NAME.equals(name)) {
            return NAME;
        }
        return name + CAPITALIZED_NAME;
    }

    private static String androidTestVariantToSourceSetName(String name) {
        if (name.endsWith(ANDROID_TEST)) {
            return Character.toLowerCase(ANDROID_TEST.charAt(0))
                    + ANDROID_TEST.substring(1)
                    + Character.toUpperCase(name.charAt(0))
                    + name.substring(1, name.length() - ANDROID_TEST.length());
        }
        return name;
    }
}
