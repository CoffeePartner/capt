package com.dieyidezui.lancet.plugin.process;

import com.dieyidezui.lancet.plugin.api.Arguments;
import com.dieyidezui.lancet.plugin.api.Plugin;
import com.dieyidezui.lancet.plugin.dsl.LancetPluginExtension;
import com.dieyidezui.lancet.plugin.gradle.GradleLancetExtension;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommonArgs {

    private Map<String, SimpleArgs> map;

    CommonArgs(Map<String, SimpleArgs> map) {
        this.map = map;
    }

    public static CommonArgs createBy(GradleLancetExtension extension, int scope, Map<String, Plugin> plugins) {
        return new CommonArgs(extension.getPlugins().stream()
                .map(e -> new SimpleArgs(e, plugins.get(e.getName()).defaultPriority()))
                .filter(a -> a.allow(scope))
                .collect(Collectors.toMap(SimpleArgs::name, Function.identity()))
        );
    }

    public Arguments asArgumentsFor(String pluginId) {
        Arguments.PluginArguments my = map.get(pluginId);
        return new Arguments() {
            @Override
            public PluginArguments getArgumentsById(String id) {
                return map.get(id);
            }

            @Override
            public PluginArguments getMyArguments() {
                return my;
            }
        };
    }


    static class SimpleArgs implements Arguments.PluginArguments {

        private final int priority;
        private final Map<String, Object> map;
        private final String name;
        private final int scope;

        SimpleArgs(LancetPluginExtension extension, int defaultPriority) {
            this.scope = extension.getScope();
            this.name = extension.getName();
            this.priority = (extension.getPriority() == null ? defaultPriority : extension.getPriority());
            this.map = extension.getPluginProperties();
        }

        boolean allow(int scope) {
            return (this.scope & scope) != 0;
        }

        public String name() {
            return name;
        }

        @Override
        public int priority() {
            return priority;
        }

        @Override
        public Map<String, Object> arguments() {
            return map;
        }
    }
}
