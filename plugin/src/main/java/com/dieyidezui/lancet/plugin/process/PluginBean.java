package com.dieyidezui.lancet.plugin.process;

public class PluginBean {

    private String id;

    public PluginBean(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "PluginBean{" +
                "id='" + id + '\'' +
                '}';
    }
}
