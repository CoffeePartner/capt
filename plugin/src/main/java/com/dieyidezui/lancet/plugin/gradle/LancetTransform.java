package com.dieyidezui.lancet.plugin.gradle;

import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.dieyidezui.lancet.plugin.util.Constants;
import com.dieyidezui.lancet.plugin.variant.VariantManager;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.IOException;
import java.util.Set;

public class LancetTransform extends Transform implements Constants {

    private static final Logger LOGGER = Logging.getLogger(LancetTransform.class);
    private static final Set<QualifiedContent.Scope> ALL = ImmutableSet.of(
            QualifiedContent.Scope.PROJECT,
            QualifiedContent.Scope.SUB_PROJECTS,
            QualifiedContent.Scope.EXTERNAL_LIBRARIES,
            QualifiedContent.Scope.PROVIDED_ONLY,
            QualifiedContent.Scope.TESTED_CODE);

    private final VariantManager variantManager;

    public LancetTransform(VariantManager variantManager) {
        this.variantManager = variantManager;
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

    /**
     * Needs other scopes to compute frame
     */
    @Override
    public Set<? super QualifiedContent.Scope> getReferencedScopes() {
        return Sets.difference(ALL, getScopes());
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
        long startMs = System.currentTimeMillis();
        LOGGER.lifecycle("Start lancet transform for '{}', incremental: {}", invocation.getContext().getVariantName(), invocation.isIncremental());
        variantManager.getVariantScope(invocation.getContext().getVariantName())
                .doTransform(invocation);
        LOGGER.lifecycle("End lancet transform, cost: {}ms", (System.currentTimeMillis() - startMs));
    }

}
