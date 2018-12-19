package com.dieyidezui.lancet.plugin.api;

import java.util.Map;

public interface Arguments {

    PluginArguments getArgumentsById(String id);

    PluginArguments getMyArguments();

    interface PluginArguments {

        /**
         * @return priority of plugi
         */
        int priority();

        /**
         * build.gradle passed arguments for specific plugin
         */
        Map<String, Object> arguments();
    }
}
