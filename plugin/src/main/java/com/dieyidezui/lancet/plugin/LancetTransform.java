package com.dieyidezui.lancet.plugin;

import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.dieyidezui.lancet.plugin.cache.DirCache;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executor;

public class LancetTransform extends Transform {

    private static final Logger LOGGER = LoggerFactory.getLogger(LancetTransform.class);
    static final String NAME = "lancet";

    private DirCache mCache;

    public LancetTransform(DirCache mCache) {
        this.mCache = mCache;
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
        return TransformManager.SCOPE_FULL_PROJECT;
    }


    @Override
    public Collection<File> getSecondaryDirectoryOutputs() {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    @Override
    public void transform(TransformInvocation invocation) throws TransformException, InterruptedException, IOException {
        if (mCache.isCacheUseful() && invocation.isIncremental()) {
            // try incremental mode
            if (mCache.await()) {
                // incremental mode

            } else {
                LOGGER.warn("Load cache failed, use full mode");
            }
        }

        // full mode
        // parse


        // diff


        // weave
    }
}
