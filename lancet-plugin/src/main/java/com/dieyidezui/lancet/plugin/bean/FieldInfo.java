package com.dieyidezui.lancet.plugin.bean;

/**
 * Created by gengwanpeng on 17/5/11.
 */
public class FieldInfo {

    public int access;
    public String name;
    public String desc;

    public FieldInfo(int access, String name, String desc) {
        this.access = access;
        this.name = name;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "FieldInfo{" +
                "access=" + access +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
