package com.dieyidezui.lancet.plugin.api;

import java.util.Map;

public interface Arguments {

    PluginArguments getArgumentsById(String id);

    PluginArguments getMyArguments();

    interface PluginArguments {

        int priority();

        /**
         * build.gradle passed arguments for specific plugin
         */
        Map<String, Object> arguments();
    }
}
