package com.dieyidezui.lancet.plugin.process;

import com.dieyidezui.lancet.plugin.api.Arguments;
import com.dieyidezui.lancet.plugin.dsl.LancetPluginExtension;
import com.dieyidezui.lancet.plugin.gradle.GradleLancetExtension;

import java.util.HashMap;
import java.util.Map;

public class CommonArgs {

    private Map<String, SimpleArgs> map;

    public CommonArgs(Map<String, SimpleArgs> map) {
        this.map = map;
    }

    public static CommonArgs createFromExtension(GradleLancetExtension extension, int scope) {

        return map
    }


    private static class SimpleArgs implements Arguments.PluginArguments {

        private final int priority;
        private final Map<String, Object> map;
        private final String name;
        private final int scope;

        public SimpleArgs(LancetPluginExtension extension) {
            this.scope = extension.getScope();
            this.name = extension.getName();
            this.priority = extension.getPriority();
            this.map = extension.getPluginProperties();
        }

        public boolean allow(int scope) {
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
