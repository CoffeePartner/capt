package com.dieyidezui.lancet.plugin.resource;

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.TransformInvocation;
import com.dieyidezui.lancet.plugin.cache.OutputProviderFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class FileManager {
    private final File variantRoot;
    private TransformInvocation invocation;

    public FileManager(File variantRoot) {
        this.variantRoot = variantRoot;
    }

    public void attachContext(TransformInvocation invocation) throws IOException {
        this.invocation = invocation;
        if (!invocation.isIncremental()) {
            clearForFullMode();
        }
    }

    public OutputProviderFactory.RootSelector asSelector() {
        return (type, id) -> {
            switch (type) {
                case CLASS:
                    return classRootFor(id);
                case TEMP:
                    return tempRootFor(id);
                case CACHE:
                    cacheRootFor(id);
            }
            throw new AssertionError();
        };
    }

    public File variantRoot() {
        return variantRoot;
    }

    private File classRootFor(String id) {
        return invocation.getOutputProvider().getContentLocation("lancet:" + id,
                Collections.singleton(QualifiedContent.DefaultContentType.CLASSES),
                Collections.singleton(QualifiedContent.Scope.EXTERNAL_LIBRARIES),
                Format.DIRECTORY);
    }

    private File tempRootFor(String id) {
        return new File(invocation.getContext().getTemporaryDir(), id);
    }


    private File cacheRootFor(String id) {
        return new File(variantRoot, "plugin_cache" + File.separator + id);
    }

    private void clearForFullMode() throws IOException {
        invocation.getOutputProvider().deleteAll();
        if (!variantRoot.exists()) {
            FileUtils.cleanDirectory(variantRoot);
        }
    }
}
