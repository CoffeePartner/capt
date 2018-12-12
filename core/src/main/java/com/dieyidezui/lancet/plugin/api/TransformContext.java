package com.dieyidezui.lancet.plugin.api;

public interface TransformContext {

    PluginContext getPluginContext();

    void chooseTo(Action action);

    enum Action {
        SKIP_THIS,
        SKIP_ALL,
        REMOVE_CLASS,
    }
}
