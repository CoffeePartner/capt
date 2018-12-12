package com.dieyidezui.lancet.plugin.gradle;

import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.dieyidezui.lancet.plugin.LancetLoader;
import com.dieyidezui.lancet.plugin.util.Constants;
import com.google.common.collect.ImmutableSet;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

public class LancetTransform extends Transform implements Constants {

    private static final Logger LOGGER = Logging.getLogger(LancetTransform.class);
    private final LancetLoader maker;

    public LancetTransform(LancetLoader maker) {
        this.maker = maker;
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
        return maker.isApplication() ? TransformManager.SCOPE_FULL_PROJECT : TransformManager.PROJECT_ONLY;
    }

    @Override
    public Collection<File> getSecondaryDirectoryOutputs() {
        return Collections.emptyList();
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    @Override
    public void transform(TransformInvocation invocation) throws TransformException, InterruptedException, IOException {

        maker.beforeTransform(invocation);

        invocation.getInputs()
                .stream()
                .flatMap(i-> Stream.<QualifiedContent>concat(i.getDirectoryInputs().stream(), i.getJarInputs().stream()))
                .forEach(i -> {
                    LOGGER.error(i.toString());
                });

        LOGGER.error("------------------------");
        invocation.getReferencedInputs()
                .stream()
                .flatMap(i-> Stream.<QualifiedContent>concat(i.getDirectoryInputs().stream(), i.getJarInputs().stream()))
                .forEach(i -> {
                    LOGGER.error(i.toString());
                });
        // full mode
        // parse


        // diff


        // weave
    }
}
