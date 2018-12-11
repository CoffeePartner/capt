
package com.dieyidezui.lancet.plugin.gradle;

import com.dieyidezui.lancet.plugin.dsl.LancetPluginExtension;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;

import javax.inject.Inject;

public class GradleLancetExtension {

    private NamedDomainObjectContainer<LancetPluginExtension> plugins;
    private boolean autoRemoveCore = true;

    @Inject
    public GradleLancetExtension(NamedDomainObjectContainer<LancetPluginExtension> plugins) {
        this.plugins = plugins;
    }

    public void plugins(Action<NamedDomainObjectContainer<LancetPluginExtension>> action) {
        action.execute(plugins);
    }

    public NamedDomainObjectContainer<LancetPluginExtension> getPlugins() {
        return plugins;
    }

    public void autoRemoveCore(boolean autoRemoveCore) {
        this.autoRemoveCore = autoRemoveCore;
    }

    public boolean isAutoRemoveCore() {
        return autoRemoveCore;
    }
}