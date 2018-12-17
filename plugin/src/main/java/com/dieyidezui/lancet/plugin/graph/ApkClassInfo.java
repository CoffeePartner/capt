package com.dieyidezui.lancet.plugin.graph;

import com.dieyidezui.lancet.plugin.api.Status;
import com.dieyidezui.lancet.plugin.api.graph.ClassInfo;
import com.dieyidezui.lancet.plugin.api.graph.MethodInfo;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ApkClassInfo implements ClassInfo {

    public Status status;
    public ClassBean clazz;

    /**
     * null means removed or is {@link Object}
     */
    @Nullable
    public ApkClassInfo parent;
    public List<ApkClassInfo> interfaces = Collections.emptyList();

    // rev direction
    public List<ApkClassInfo> classChildren = Collections.emptyList();
    public List<ApkClassInfo> interfaceChildren = Collections.emptyList();
    public List<ApkClassInfo> implementedClasses = Collections.emptyList();

    public static ApkClassInfo createStub(String name) {
        return new ApkClassInfo(new ClassBean(name));
    }

    private ApkClassInfo(ClassBean clazz) {
        this.clazz = clazz;
        this.status = Status.NOT_EXISTS;
    }

    @Override
    public Status status() {
        return status;
    }

    @Override
    public int access() {
        return clazz.access;
    }

    @Override
    public String name() {
        return clazz.name;
    }

    @Override
    public Class<?> loadClass() {
        // TODO
        return null;
    }

    @Nullable
    @Override
    public String signature() {
        return clazz.signature;
    }

    @Nullable
    @Override
    public ClassInfo parent() {
        return parent;
    }

    @Override
    public List<? extends MethodInfo> methods() {
        return clazz.methods;
    }

    @Override
    public List<? extends ClassInfo> interfaces() {
        return interfaces;
    }

    @Override
    public List<? extends ClassInfo> classChildren() {
        return classChildren;
    }

    @Override
    public List<? extends ClassInfo> interfaceChildren() {
        return interfaceChildren;
    }

    @Override
    public List<? extends ClassInfo> implementedClasses() {
        return implementedClasses;
    }

    @Override
    public boolean exists() {
        return status != Status.NOT_EXISTS && status != Status.REMOVED;
    }
}
