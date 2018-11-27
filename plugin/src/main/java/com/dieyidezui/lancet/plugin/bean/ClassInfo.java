package com.dieyidezui.lancet.plugin.bean;


import java.util.ArrayList;
import java.util.List;

public class ClassInfo {

    public int access;
    public String name;
    public String superName;
    public List<String> interfaces;

    // Remove field info because useless till now.
    //public List<FieldInfo> fields = new ArrayList<>();

    public List<MethodInfo> methods = new ArrayList<>();
}
