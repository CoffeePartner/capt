package com.dieyidezui.lancet.plugin;

import com.android.build.api.attributes.BuildTypeAttr;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.LibraryVariant;
import com.android.builder.model.SourceProvider;
import com.dieyidezui.lancet.plugin.util.Constants;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.Usage;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.SourceSet;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.stream.Stream;

public class LancetLoader implements Constants {

    private static final Logger LOGGER = Logging.getLogger(LancetLoader.class);
    private static final Attribute<String> ARTIFACT_TYPE = Attribute.of("artifactType", String.class);

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

        LOGGER.error(Arrays.toString(lancetDependencies.getURLs()));
        //LOGGER.error(Arrays.toString(runtimeUrls));
    }

    public boolean isApplication() {
        return extension instanceof AppExtension;
    }

    public void createConfigurationForVariant() {
        ConfigurationContainer configurations = project.getConfigurations();
        extension.getSourceSets().all(androidSourceSet -> {
            if (!androidSourceSet.getName().startsWith(TEST)) { // don't support test
                Configuration configuration = configurations.maybeCreate(computeConfigurationName(androidSourceSet.getName()));
                configuration.setVisible(false);
                configuration.setCanBeConsumed(false);
            }
        });

        DomainObjectCollection<?> collection = extension instanceof AppExtension ?
                ((AppExtension) extension).getApplicationVariants()
                : ((LibraryExtension) extension).getLibraryVariants();

        final Usage runtimeUsage = project.getObjects().named(Usage.class, Usage.JAVA_RUNTIME);

        collection.all(v -> configure(runtimeUsage,
                (isApplication() ? ((ApplicationVariant) v).getTestVariant() : ((LibraryVariant) v).getTestVariant()),
                configure(runtimeUsage, (BaseVariant) v, null))
        );
    }

    /**
     * pre is not null means TestVariant
     */
    private Configuration configure(Usage runtime, @Nullable BaseVariant v, @Nullable Configuration pre) {
        if (v == null) {
            return null;
        }
        ConfigurationContainer configurations = project.getConfigurations();
        String name = pre == null ? v.getName() : androidTestVariantToSourceSetName(v.getName());
        Configuration cur = project.getConfigurations().getByName(computeConfigurationName(name));
        if (pre != null) {
            cur.extendsFrom(pre);
        }
        v.getSourceSets().stream()
                .map(SourceProvider::getName)
                .map(LancetLoader::computeConfigurationName)
                .map(configurations::getByName)
                .filter(c -> c != cur)
                .forEach(cur::extendsFrom);

        cur.getAttributes()
                .attribute(ARTIFACT_TYPE, ArtifactTypeDefinition.JAR_TYPE)
                .attribute(BuildTypeAttr.ATTRIBUTE, project.getObjects().named(BuildTypeAttr.class, v.getBuildType().getName()))
                .attribute(Usage.USAGE_ATTRIBUTE, runtime);

        return cur;
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
