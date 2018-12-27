package com.dieyidezui.lancet.plugin.graph;

import com.dieyidezui.lancet.plugin.api.graph.Status;
import com.dieyidezui.lancet.plugin.api.graph.ClassGraph;
import com.dieyidezui.lancet.plugin.resource.VariantResource;
import com.dieyidezui.lancet.plugin.util.ConcurrentHashSet;
import com.dieyidezui.lancet.plugin.util.Constants;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ApkClassGraph implements ClassGraph {

    private static final Logger LOGGER = Logging.getLogger(ApkClassGraph.class);

    private final Map<String, ApkClassInfo> classes = new ConcurrentHashMap<>(Constants.OPT_SIZE);
    private final VariantResource variantResource;
    private final boolean throwIfDuplicated;
    private Set<String> removedJars = new ConcurrentHashSet<>();

    public ApkClassGraph(VariantResource variantResource, boolean throwIfDuplicated) {
        this.variantResource = variantResource;
        this.throwIfDuplicated = throwIfDuplicated;
    }

    public Map<String, ApkClassInfo> getAll() {
        return classes;
    }

    @SuppressWarnings("Convert2Lambda")
    public Consumer<Classes> readClasses() {
        // Don't use lambda here, or you will lose generic info
        return new Consumer<Classes>() {
            @Override
            public void accept(Classes classes) {
                classes.classes.parallelStream()
                        .forEach(c -> ApkClassGraph.this.add(c, Status.NOT_CHANGED));
            }
        };
    }

    @SuppressWarnings("Convert2Lambda")
    public Supplier<Classes> writeClasses() {
        return new Supplier<Classes>() {
            @Override
            public Classes get() {
                return new Classes(classes.values().parallelStream()
                        .filter(ApkClassInfo::exists)
                        .map(n -> n.clazz)
                        .collect(Collectors.toCollection(() -> new ArrayList<>(classes.size() >> 3))));
            }
        };
    }


    public void onJarRemoved(String name) {
        removedJars.add(name);
    }


    public void markRemovedClasses() {
        Set<String> removed = new HashSet<>(removedJars);
        this.getAll()
                .values()
                .parallelStream()
                .filter(c -> c.status() == Status.NOT_CHANGED && removed.contains(c.clazz.belongsTo))
                // make sure mark remove succeed, so pass null is OK
                .forEach(c -> c.markRemoved(throwIfDuplicated, null));
    }

    public void add(ClassBean clazz, Status status) {
        ApkClassInfo node = getOrCreate(clazz.name, (clazz.access & Opcodes.ACC_INTERFACE) != 0);
        node.update(clazz, status, throwIfDuplicated);
        if (clazz.superName != null) {
            node.parent = getOrCreate(clazz.superName, false);

        }
        if (!clazz.interfaces.isEmpty()) {
            node.interfaces = clazz.interfaces.stream().map(n -> getOrCreate(n, true)).collect(Collectors.toList());
        }
    }

    private ApkClassInfo getOrCreate(String name, boolean isInterface) {
        return classes.computeIfAbsent(name, n -> ApkClassInfo.createStub(variantResource, name, isInterface));
    }

    @Nullable
    @Override
    public ApkClassInfo get(String name) {
        return classes.get(name);
    }

    public void markRemoved(String className, String belongsTo) {
        classes.get(className).markRemoved(throwIfDuplicated, belongsTo);
    }


    public static class Classes {
        public List<ClassBean> classes;

        public Classes(List<ClassBean> classes) {
            this.classes = classes;
        }
    }
}
