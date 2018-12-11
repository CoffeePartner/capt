package com.dieyidezui.lancet.plugin;

import com.dieyidezui.lancet.core.LancetPlugin;
import com.dieyidezui.lancet.plugin.gradle.GradleLancetExtension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PluginManager {

    Map<String, LancetPlugin> plugins = new HashMap<>();
    Set<String> definedInApk = new HashSet<>();

    public void findPlugins(GradleLancetExtension extension, LancetLoader loader) {
        extension.getPlugins().all(e -> {

        });
    }
}
