package com.dieyidezui.capt.plugin.gradle;

import com.dieyidezui.capt.plugin.dsl.CaptPluginExtension;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;

import javax.inject.Inject;

public class GradleCaptExtension {

    private NamedDomainObjectContainer<CaptPluginExtension> plugins;

    private boolean throwIfDuplicated = true;

    @Inject
    public GradleCaptExtension(NamedDomainObjectContainer<CaptPluginExtension> plugins) {
        this.plugins = plugins;
    }

    public void plugins(Action<NamedDomainObjectContainer<CaptPluginExtension>> action) {
        action.execute(plugins);
    }

    public NamedDomainObjectContainer<CaptPluginExtension> getPlugins() {
        return plugins;
    }

    public void setThrowIfDuplicated(boolean throwIfDuplicated) {
        this.throwIfDuplicated = throwIfDuplicated;
    }

    public void throwIfDuplicated(boolean throwIfDuplicated) {
        setThrowIfDuplicated(throwIfDuplicated);
    }

    public boolean getThrowIfDuplicated() {
        return throwIfDuplicated;
    }
}