package coffeepartner.capt.plugin.process;

import coffeepartner.capt.plugin.api.Arguments;
import coffeepartner.capt.plugin.api.Plugin;
import coffeepartner.capt.plugin.dsl.CaptPluginExtension;
import coffeepartner.capt.plugin.gradle.GradleCaptExtension;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommonArgs {

    private Map<String, SimpleArgs> map;

    CommonArgs(Map<String, SimpleArgs> map) {
        this.map = map;
    }

    public static CommonArgs createBy(GradleCaptExtension extension, int scope, Map<String, Plugin> plugins) {
        return new CommonArgs(extension.getPlugins().stream()
                .map(e -> {
                    Plugin plugin = plugins.get(e.getName());
                    if (plugin == null) {
                        return null;
                    }
                    return new SimpleArgs(e, plugins.get(e.getName()).defaultPriority());
                })
                .filter(a -> {
                    if (a != null) {
                        if (a.allow(scope)) {
                            return true;
                        }
                        plugins.remove(a.name());
                    }
                    return false;
                })
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

        SimpleArgs(CaptPluginExtension extension, int defaultPriority) {
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
