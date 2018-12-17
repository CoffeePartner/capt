package com.dieyidezui.lancet.plugin.graph;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClassBean {

    public int access;
    public String name;
    @Nullable
    public String signature;
    @Nullable
    public String superName;
    public List<String> interfaces;

    // remove field because useless till now.
    //public List<FieldBean> fields = new ArrayList<>();

    public List<MethodBean> methods = new ArrayList<>();


    public ClassBean(String name) {
        this(0, name, null, null);
    }


    public ClassBean(int access, String name, @Nullable String signature, @Nullable String superName) {
        this.access = access;
        this.name = name;
        this.signature = signature;
        this.superName = superName;
        this.interfaces = Collections.emptyList();
        this.methods = Collections.emptyList();
    }

    public void addMethod(MethodBean methodBean) {
        if (methods == Collections.<MethodBean>emptyList()) {
            methods = new ArrayList<>();
        }
        methods.add(methodBean);
    }

    @Override
    public String toString() {
        return "ClassBean{" +
                "access=" + access +
                ", name='" + name + '\'' +
                ", signature='" + signature + '\'' +
                ", superName='" + superName + '\'' +
                ", interfaces=" + interfaces +
                ", methods=" + methods +
                '}';
    }
}
