package com.dieyidezui.lancet.plugin.variant;

import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.api.BaseVariant;
import com.dieyidezui.lancet.plugin.cache.OutputProviderFactory;
import com.dieyidezui.lancet.plugin.cache.RelativeDirectoryProviderFactory;
import com.dieyidezui.lancet.plugin.cache.RelativeDirectoryProviderFactoryImpl;
import com.dieyidezui.lancet.plugin.lancetplugin.PluginManager;
import com.dieyidezui.lancet.plugin.resource.FileManager;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;
import com.dieyidezui.lancet.plugin.resource.VariantResource;
import com.dieyidezui.lancet.plugin.util.Constants;
import org.gradle.api.artifacts.Configuration;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;


public class VariantScope implements Constants {

    private final String variant;
    private Configuration lancetConfiguration;
    private final GlobalResource global;
    private final FileManager files;

    VariantScope(String variant, Configuration lancetConfiguration, GlobalResource global) {
        this.variant = variant;
        this.lancetConfiguration = lancetConfiguration;
        this.global = global;
        this.files = new FileManager(new File(global.root(), getVariant()));
    }

    public Configuration getLancetConfiguration() {
        return lancetConfiguration;
    }

    public File getRoot() {
        return files.root();
    }

    public String getVariant() {
        return variant;
    }

    public void doTransform(TransformInvocation invocation) throws IOException, TransformException, InterruptedException {

        RelativeDirectoryProviderFactory singleFactory = new RelativeDirectoryProviderFactoryImpl();

        OutputProviderFactory factory = new OutputProviderFactory(singleFactory, files.asSelector());

        VariantResource variantResource = new VariantResource(
                files, factory);

        variantResource.prepare(invocation, getLancetConfiguration());

        PluginManager manager = new PluginManager();
    }

    public interface Factory {

        VariantScope create(BaseVariant v);

        VariantScope create(BaseVariant v, @Nullable VariantScope parent);
    }
}
