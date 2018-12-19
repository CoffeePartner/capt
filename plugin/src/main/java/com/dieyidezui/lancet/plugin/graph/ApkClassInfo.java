package com.dieyidezui.lancet.plugin.graph;

import com.dieyidezui.lancet.plugin.api.graph.Status;
import com.dieyidezui.lancet.plugin.api.graph.ClassInfo;
import com.dieyidezui.lancet.plugin.api.graph.MethodInfo;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ApkClassInfo implements ClassInfo {

    private static final Logger LOGGER = Logging.getLogger(ApkClassInfo.class);

    private AtomicReference<Status> status;
    public ClassBean clazz;

    /**
     * null means removed or not exists
     */
    @Nullable
    public ApkClassInfo parent;
    public List<ApkClassInfo> interfaces = Collections.emptyList();

    // rev direction
    public List<ApkClassInfo> classChildren = Collections.emptyList();
    public List<ApkClassInfo> interfaceChildren = Collections.emptyList();
    public List<ApkClassInfo> implementedClasses = Collections.emptyList();

    public static ApkClassInfo createStub(String name, boolean isInterface) {
        return new ApkClassInfo(new ClassBean(name, isInterface));
    }

    private ApkClassInfo(ClassBean clazz) {
        this.clazz = clazz;
        this.status = new AtomicReference<>(Status.NOT_EXISTS);
    }

    public void markRemoved() {
        status.set(Status.REMOVED);
        parent = null;
        interfaces = classChildren = interfaceChildren = implementedClasses = Collections.emptyList();
    }

    void updateStatus(Status newStatus, boolean throwIfDuplicated) {
        Status old = this.status.getAndSet(newStatus);
        if (old != Status.NOT_EXISTS) {
            // remove && add ==  changed
            if (old == Status.ADDED && newStatus == Status.REMOVED
                    || old == Status.REMOVED && newStatus == Status.ADDED) {
                Status pre = this.status.getAndSet(Status.CHANGED);
                if (pre == newStatus) {
                    return;
                }
            }
            if (throwIfDuplicated) {
                throw new IllegalStateException("Duplicated class: " + clazz.name);
            }
            LOGGER.error("Duplicated class: " + clazz.name);
        }
    }


    @Override
    public Status status() {
        return status.get();
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
        Status s = status.get();
        return s != Status.NOT_EXISTS && s != Status.REMOVED;
    }
}
