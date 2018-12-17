package com.dieyidezui.lancet.plugin.gradle;

import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.dieyidezui.lancet.plugin.lancetplugin.PluginManager;
import com.dieyidezui.lancet.plugin.resource.ResourceManager;
import com.dieyidezui.lancet.plugin.util.Constants;
import com.dieyidezui.lancet.plugin.variant.VariantManager;
import com.google.common.collect.ImmutableSet;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

public class LancetTransform extends Transform implements Constants {

    private static final Logger LOGGER = Logging.getLogger(LancetTransform.class);
    private final VariantManager variantManager;
    private final PluginManager pluginManager;
    private final ResourceManager resourceManager;

    public LancetTransform(ResourceManager resourceManager, VariantManager variantManager, PluginManager pluginManager) {
        this.resourceManager = resourceManager;
        this.variantManager = variantManager;
        this.pluginManager = pluginManager;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return variantManager.isApplication() ? TransformManager.SCOPE_FULL_PROJECT : TransformManager.PROJECT_ONLY;
    }

    @Override
    public Collection<File> getSecondaryDirectoryOutputs() {
        return ImmutableSet.of(resourceManager.getLancetRoot());
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    @Override
    public boolean isCacheable() {
        return true;
    }

    @Override
    public void transform(TransformInvocation invocation) throws TransformException, InterruptedException, IOException {
        resourceManager.prepare(invocation, variantManager.getByVariant(invocation.getContext().getVariantName()));

        pluginManager.findPlugins(resourceManager.getPluinArgs());

        invocation.getInputs()
                .stream()
                .flatMap(i -> Stream.<QualifiedContent>concat(i.getDirectoryInputs().stream(), i.getJarInputs().stream()))
                .forEach(i -> {
                    //   LOGGER.error(i.toString());
                });

        LOGGER.error("------------------------");
        invocation.getReferencedInputs()
                .stream()
                .flatMap(i -> Stream.<QualifiedContent>concat(i.getDirectoryInputs().stream(), i.getJarInputs().stream()))
                .forEach(i -> {
                    // LOGGER.error(i.toString());
                });
        // full mode
        // parse


        // diff


        // weave
    }
}
