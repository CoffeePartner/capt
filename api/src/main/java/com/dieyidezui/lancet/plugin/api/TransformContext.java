package com.dieyidezui.lancet.plugin.api;

public interface TransformContext {

    Lancet getPluginContext();

    void chooseTo(Action action);

    enum Action {
        SKIP_THIS,
        SKIP_ALL,
        REMOVE_CLASS,
    }
}
