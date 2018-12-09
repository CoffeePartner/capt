package com.dieyidezui.lancet.plugin.dsl;

import groovy.lang.GroovyObjectSupport;
import org.gradle.api.Named;

import java.util.Map;

public class LancetPluginExtension extends GroovyObjectSupport implements Named {

    private final String name;
    private Integer priority = null;
    private ConfigurableMap configurableMap = new ConfigurableMap();

    public LancetPluginExtension(String name) {
        this.name = name;
    }

    @Override
    public Object invokeMethod(String name, Object args) {
        return configurableMap.invokeMethod(name, args);
    }

    @Override
    public void setProperty(String property, Object newValue) {
        configurableMap.setProperty(property, newValue);
    }

    @Override
    public Object getProperty(String property) {
        return configurableMap.getProperty(property);
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void priority(int priority) {
        setPriority(priority);
    }

    public Map<String, Object> getPluginProperties() {
        return configurableMap.toRealMap();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "LancetPluginExtension{" +
                "name='" + name + '\'' +
                ", priority=" + priority +
                ", configurableMap=" + configurableMap +
                '}';
    }
}
