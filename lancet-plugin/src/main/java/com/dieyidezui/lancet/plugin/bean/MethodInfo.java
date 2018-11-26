package com.dieyidezui.lancet.plugin.bean;

/**
 * Created by gengwanpeng on 17/5/11.
 */
public class MethodInfo {

    public int access;
    public String name;
    public String desc;

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
