package com.dieyidezui.lancet.plugin.variant;

import com.android.build.gradle.api.BaseVariant;
import org.gradle.api.artifacts.Configuration;

import javax.annotation.Nullable;


public class VariantDependencies {

    private Configuration lancetConfiguration;

    VariantDependencies(Configuration lancetConfiguration) {
        this.lancetConfiguration = lancetConfiguration;
    }

    public Configuration getLancetConfiguration() {
        return lancetConfiguration;
    }


    public interface Factory {

        VariantDependencies create(BaseVariant v);

        VariantDependencies create(BaseVariant v, @Nullable VariantDependencies parent);
    }
}
