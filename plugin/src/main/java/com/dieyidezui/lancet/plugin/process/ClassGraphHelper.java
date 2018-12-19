package com.dieyidezui.lancet.plugin.process;


import com.dieyidezui.lancet.plugin.api.graph.Status;
import com.dieyidezui.lancet.plugin.graph.ApkClassGraph;
import com.dieyidezui.lancet.plugin.graph.ApkClassInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ClassGraphHelper {
    private final ApkClassGraph graph;
    private Set<String> preMetas = Collections.emptySet();
    private Map<String, ClassGraphHelper> metas = new ConcurrentHashMap<>();
    private Map<String, ClassGraphHelper> removedJars = new ConcurrentHashMap<>();
    private List<ApkClassInfo> removedClasses;

    public ClassGraphHelper(ApkClassGraph graph) {
        this.graph = graph;
    }


    public Consumer<MetaClasses> readPreMetas() {
        return m -> preMetas = m.classes;
    }

    public Supplier<MetaClasses> writeMetas() {
        return () -> new MetaClasses(metas.keySet());
    }

    public void addMeta(String meta) {
        metas.put(meta, this);
    }

    public void onJarRemoved(String name) {
        removedJars.put(name, this);
    }

    public void dispatchMetas() {
    }

    public void collectRemovedClasses() {
        Set<String> removed = new HashSet<>(removedJars.keySet());
        removedClasses = graph.getAll()
                .values()
                .parallelStream()
                // on removed directory, status == REMOVED
                // on changed/removed jar ,but class is NOT_CHANGED, means the class is removed
                .filter(c -> {
                    if (c.status() == Status.REMOVED) {
                        return true;
                    }
                    if (c.status() == Status.NOT_CHANGED && removed.contains(c.clazz.belongsTo)) {
                        c.markRemoved();
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    public List<ApkClassInfo> removedClasses() {
        return removedClasses;
    }

    public static class MetaClasses {

        public MetaClasses(Set<String> classes) {
            this.classes = classes;
        }

        Set<String> classes;
    }

}
