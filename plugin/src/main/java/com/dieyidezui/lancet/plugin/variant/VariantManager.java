package com.dieyidezui.lancet.plugin.variant;

import com.android.build.api.attributes.BuildTypeAttr;
import com.android.build.api.attributes.ProductFlavorAttr;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.LibraryVariant;
import com.android.build.gradle.api.TestVariant;
import com.android.build.gradle.internal.pipeline.TransformTask;
import com.android.builder.model.SourceProvider;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;
import com.dieyidezui.lancet.plugin.util.Constants;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ResolutionStrategy;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.Usage;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.SourceSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Cross multi variant tasks
 */
public class VariantManager implements Constants {

    private static final Logger LOGGER = Logging.getLogger(VariantManager.class);
    private static final Attribute<String> ARTIFACT_TYPE = Attribute.of("artifactType", String.class);

    private final Map<String, VariantScope> dependencies = new HashMap<>();
    private VariantScope.Factory factory;

    private final GlobalResource global;
    private final BaseExtension extension;
    private final Project project;

    public VariantManager(GlobalResource global, BaseExtension extension, Project project) {
        this.global = global;
        this.extension = extension;
        this.project = project;
        this.factory = new VariantDependenciesFactory();
    }

    public boolean isApplication() {
        return extension instanceof AppExtension;
    }

    public void createConfigurationForVariant() {
        ConfigurationContainer configurations = project.getConfigurations();
        extension.getSourceSets().all(androidSourceSet -> {
            if (!androidSourceSet.getName().startsWith(TEST)) { // don't support unit test
                Configuration configuration = configurations.maybeCreate(sourceSetToConfigurationName(androidSourceSet.getName()));
                // internal use, will be extended by the actual variant configuration
                configuration.setDescription("Classpath for the lancet plugin for " + androidSourceSet.getName() + ".");
                configuration.setVisible(false);
                configuration.setCanBeConsumed(false);
                configuration.setCanBeResolved(false);
            }
        });

        DomainObjectCollection<? extends BaseVariant> collection = extension instanceof AppExtension ?
                ((AppExtension) extension).getApplicationVariants()
                : ((LibraryExtension) extension).getLibraryVariants();

        collection.all(v -> {
            VariantScope variant = factory.create(v);
            dependencies.put(v.getName(), variant);

            TestVariant t;
            if (v instanceof ApplicationVariant) {
                t = ((ApplicationVariant) v).getTestVariant();
            } else {
                t = ((LibraryVariant) v).getTestVariant();
            }

            if (t != null) {
                VariantScope testVariant = factory.create(t, variant);
                dependencies.put(t.getName(), testVariant);
            }
        });


        // The fucking transform API doesn't provide evaluating time variant!
        project.afterEvaluate(p -> p.getTasks()
                .withType(TransformTask.class, t -> {
                    if (t.getTransform().getName().equals(NAME)) {
                        t.dependsOn(getByVariant(t.getVariantName()));

                        // register output
                        t.getOutputs().dir(getVariantScope(t.getVariantName()).getRoot());
                    }
                }));
    }

    private Configuration getByVariant(String name) {
        return project.getConfigurations().maybeCreate(name + "LancetClasspath");
    }

    public VariantScope getVariantScope(String name) {
        return Objects.requireNonNull(dependencies.get(name));
    }

    private static String sourceSetToConfigurationName(String name) {
        if (SourceSet.MAIN_SOURCE_SET_NAME.equals(name)) {
            return NAME;
        }
        return name + CAPITALIZED_NAME;
    }

    private class VariantDependenciesFactory implements VariantScope.Factory {


        @Override
        public VariantScope create(BaseVariant v) {
            ConfigurationContainer configurations = project.getConfigurations();

            // the actual configuration
            Configuration configuration = getByVariant(v.getName());

            // attributes match
            AttributeContainer attributes = configuration.getAttributes();
            attributes
                    .attribute(ARTIFACT_TYPE, ArtifactTypeDefinition.JAR_TYPE)
                    .attribute(BuildTypeAttr.ATTRIBUTE, project.getObjects().named(BuildTypeAttr.class, v.getBuildType().getName()))
                    .attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.JAVA_RUNTIME));
            v.getProductFlavors().forEach(p -> attributes.attribute(Attribute.of(p.getDimension(), ProductFlavorAttr.class),
                    project.getObjects().named(ProductFlavorAttr.class, p.getName())));

            configuration.setDescription("Resolved configuration for lancet for variant: " + v.getName());
            configuration.setVisible(false);
            configuration.setCanBeConsumed(false);
            configuration.getResolutionStrategy().sortArtifacts(ResolutionStrategy.SortOrder.CONSUMER_FIRST);

            v.getSourceSets().stream()
                    .map(SourceProvider::getName)
                    .map(VariantManager::sourceSetToConfigurationName)
                    .map(configurations::getByName)
                    .forEach(configuration::extendsFrom);

            return new VariantScope(v.getName(), configuration, global);
        }

        @Override
        public VariantScope create(BaseVariant v, VariantScope parent) {
            VariantScope child = create(v);
            // TODO: should androidTest variant extendsFrom normal variant ?
            child.getLancetConfiguration().extendsFrom(parent.getLancetConfiguration());
            return child;
        }
    }
}
