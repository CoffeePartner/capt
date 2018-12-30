package com.dieyidezui.capt.plugin.graph;


import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClassBean {

    public String belongsTo;

    public int access;
    public String name;
    @Nullable
    public String signature;
    @Nullable
    public String superName;
    public List<String> interfaces;

    // remove field because useless till now.
    //public List<FieldBean> fields = new ArrayList<>();

    public List<MethodBean> methods = Collections.emptyList();


    public ClassBean(String name, boolean isInterface) {
        this(isInterface ? Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT : 0, name, null, null, null);
    }


    public ClassBean(int access, String name, @Nullable String signature, @Nullable String superName, @Nullable String[] interfaces) {
        this.access = access;
        this.name = name;
        this.signature = signature;
        this.superName = superName;
        this.interfaces = interfaces == null ? Collections.emptyList() : Arrays.asList(interfaces);
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
