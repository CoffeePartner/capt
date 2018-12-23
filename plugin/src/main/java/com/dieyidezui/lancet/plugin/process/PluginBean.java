package com.dieyidezui.lancet.plugin.process;

import java.util.List;

public class PluginBean {

    private String id;

    private List<String> affectedClasses;

    public PluginBean(String id, List<String> affectedClasses) {
        this.id = id;
        this.affectedClasses = affectedClasses;
    }

    public String getId() {
        return id;
    }

    public List<String> getAffectedClasses() {
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
