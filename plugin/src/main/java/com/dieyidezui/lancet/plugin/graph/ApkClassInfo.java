package com.dieyidezui.lancet.plugin.graph;

import com.dieyidezui.lancet.plugin.api.graph.ClassInfo;
import com.dieyidezui.lancet.plugin.api.graph.MethodInfo;
import com.dieyidezui.lancet.plugin.api.graph.Status;
import com.dieyidezui.lancet.plugin.resource.VariantResource;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ApkClassInfo implements ClassInfo {

    private static final Logger LOGGER = Logging.getLogger(ApkClassInfo.class);

    public AtomicReference<Status> status;
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

    private final VariantResource resource;

    public static ApkClassInfo createStub(VariantResource resource, String name, boolean isInterface) {
        return new ApkClassInfo(resource, new ClassBean(name, isInterface));
    }

    private ApkClassInfo(VariantResource resource, ClassBean clazz) {
        this.resource = resource;
        this.clazz = clazz;
        this.status = new AtomicReference<>(Status.NOT_EXISTS);
    }

    public void markRemoved(boolean throwIfDuplicated, String belongsTo) {
        if (updateStatus(Status.REMOVED)) {
            if (status.get() == Status.REMOVED) {
                parent = null;
                interfaces = classChildren = interfaceChildren = implementedClasses = Collections.emptyList();
            }
        } else {
            throwOrLog(throwIfDuplicated, belongsTo);
        }
    }

    void update(ClassBean bean, Status newStatus, boolean throwIfDuplicated) {
        if (updateStatus(newStatus)) {
            this.clazz = bean;
        } else {
            throwOrLog(throwIfDuplicated, bean.belongsTo);
        }
    }

    private void throwOrLog(boolean throwIfDuplicated, String belongsTo) {
        if (throwIfDuplicated) {
            throw new IllegalStateException("Found duplicated class: " + clazz.name + "in '" + clazz.belongsTo + "' and ' " + belongsTo + "'");
        }
        LOGGER.error("Found duplicated class: {} in '{}' and '{}'", clazz.name, clazz.belongsTo, belongsTo);
    }

    private boolean updateStatus(Status newStatus) {
        Status oldStatus = this.status.getAndSet(newStatus);
        if (oldStatus != Status.NOT_EXISTS && oldStatus != Status.NOT_CHANGED) {
            if (oldStatus == Status.ADDED && newStatus == Status.REMOVED
                    || oldStatus == Status.REMOVED && newStatus == Status.ADDED) {
                return status.getAndSet(Status.CHANGED) == newStatus;
            }
            return false;
        }
        return true;
    }


    @Override
    public Status status() {
        Status res = status.get();
        if (!resource.isIncremental()) {
            return res == Status.NOT_EXISTS || res == Status.REMOVED ? Status.NOT_EXISTS
                    : Status.NOT_CHANGED;
        }
        return res;
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
    public Class<?> loadClass() throws ClassNotFoundException {
        return resource.loadClass(name());
    }

    @Override
    public InputStream openStream() throws IOException {
        return resource.openStream(name());
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

    @Override
    public String toString() {
        return "ApkClassInfo{" +
                "status=" + status.get() +
                ", clazz=" + clazz +
                '}';
    }
}
