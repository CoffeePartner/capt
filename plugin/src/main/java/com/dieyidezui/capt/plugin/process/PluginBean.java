package com.dieyidezui.capt.plugin.process;

import java.util.Set;

public class PluginBean {

    private String id;

    private Set<String> affectedClasses;

    public PluginBean(String id, Set<String> affectedClasses) {
        this.id = id;
        this.affectedClasses = affectedClasses;
    }

    public String getId() {
        return id;
    }

    public Set<String> getAffectedClasses() {
        return affectedClasses;
    }

    @Override
    public String toString() {
        return "PluginBean{" +
                "id='" + id + '\'' +
                ", affectedClasses=" + affectedClasses +
                '}';
    }
}
