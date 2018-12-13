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
import com.android.builder.model.SourceProvider;
import com.dieyidezui.lancet.plugin.util.Constants;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ResolutionStrategy;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.Usage;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.SourceSet;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class VariantManager implements Constants {

    private static final Logger LOGGER = Logging.getLogger(VariantManager.class);
    public static final Attribute<String> ARTIFACT_TYPE = Attribute.of("artifactType", String.class);

    private Map<String, VariantDependencies> dependencies = new HashMap<>();
    private VariantDependencies.Factory factory;

    private final BaseExtension extension;
    private final Project project;

    public VariantManager(BaseExtension extension, Project project) {
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
            if (!androidSourceSet.getName().startsWith(TEST)) { // don't support test
                Configuration configuration = configurations.maybeCreate(sourceSetToConfigurationName(androidSourceSet.getName()));
                configuration.setDescription("Classpath for the annotation processor for " + androidSourceSet.getName() + ".");
                configuration.setVisible(false);
                configuration.setCanBeConsumed(false);
                configuration.setCanBeResolved(false);
            }
        });

        DomainObjectCollection<? extends BaseVariant> collection = extension instanceof AppExtension ?
                ((AppExtension) extension).getApplicationVariants()
                : ((LibraryExtension) extension).getLibraryVariants();

        collection.all(v -> {
            VariantDependencies variant = factory.create(v);
            dependencies.put(v.getName(), variant);

            TestVariant t;
            if (v instanceof ApplicationVariant) {
                t = ((ApplicationVariant) v).getTestVariant();
            } else {
                t = ((LibraryVariant) v).getTestVariant();
            }

            if (t != null) {
                VariantDependencies testVariant = factory.create(t, variant);
                dependencies.put(t.getName(), testVariant);
            }
        });
    }

    public Configuration getByVariant(String name) {
        return dependencies.get(name).getLancetConfiguration();
    }

    private static String sourceSetToConfigurationName(String name) {
        if (SourceSet.MAIN_SOURCE_SET_NAME.equals(name)) {
            return NAME;
        }
        return name + CAPITALIZED_NAME;
    }

    private class VariantDependenciesFactory implements VariantDependencies.Factory {


        @Override
        public VariantDependencies create(BaseVariant v) {
            ConfigurationContainer configurations = project.getConfigurations();
            Configuration lancet = configurations.maybeCreate(v.getName() + "LancetClasspath");
            AttributeContainer attributes = lancet.getAttributes();
            attributes
                    .attribute(BuildTypeAttr.ATTRIBUTE, project.getObjects().named(BuildTypeAttr.class, v.getBuildType().getName()))
                    .attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.JAVA_RUNTIME));
            //v.getProductFlavors().forEach(p -> attributes.attribute(Attribute.of(p.getDimension(), ProductFlavorAttr.class),
            //project.getObjects().named(ProductFlavorAttr.class, p.getName())));

            lancet.setDescription("Resolved configuration for lancet for variant: " + v.getName());
            lancet.setVisible(false);
            lancet.setCanBeConsumed(false);
            lancet.getResolutionStrategy().sortArtifacts(ResolutionStrategy.SortOrder.CONSUMER_FIRST);

            v.getSourceSets().stream()
                    .map(SourceProvider::getName)
                    .map(VariantManager::sourceSetToConfigurationName)
                    .map(configurations::getByName)
                    .forEach(lancet::extendsFrom);

            return new VariantDependencies(lancet);
        }

        @Override
        public VariantDependencies create(BaseVariant v, @Nullable VariantDependencies parent) {
            VariantDependencies child = create(v);
            if (parent != null) {
                child.getLancetConfiguration().extendsFrom(parent.getLancetConfiguration());
            }
            return child;
        }
    }
}
