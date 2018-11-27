package com.dieyidezui.lancet.plugin.bean;

import com.dieyidezui.lancet.plugin.bean.hook.MethodLocator;

import java.util.List;
import java.util.jar.JarFile;

public class MethodInfo {

    public int access;
    public String name;
    public String desc;
    public List<String> catchedTypes;
    public List<MethodLocator> invokedMethods;

    public MethodInfo(int access, String name, String desc) {
        this.access = access;
        this.name = name;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "MethodInfo{" +
                "access=" + access +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
