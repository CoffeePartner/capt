package com.dieyidezui.lancet.plugin.graph;

import com.dieyidezui.lancet.plugin.api.Status;
import com.dieyidezui.lancet.plugin.api.graph.ClassGraph;
import com.dieyidezui.lancet.plugin.api.graph.ClassInfo;
import com.dieyidezui.lancet.plugin.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ApkClassGraph implements ClassGraph {

    private final Map<String, ApkClassInfo> classes = new ConcurrentHashMap<>(Constants.OPT_SIZE);

    public ApkClassGraph() {

    }

    public Consumer<List<ClassBean>> asConsumer() {
        return list -> list.parallelStream()
                .forEach(c -> add(c, Status.NOT_CHANGED));
    }

    public Supplier<List<ClassBean>> asSupplier() {
        return () -> classes.values().stream()
                .filter(ApkClassInfo::exists)
                .map(n -> n.clazz)
                .collect(Collectors.toCollection(() -> new ArrayList<>(classes.size())));
    }

    public void add(ClassBean clazz, Status status) {
        ApkClassInfo node = getOrCreate(clazz.name);
        node.status = status;
        node.clazz = clazz;
        if (clazz.superName != null) {
            node.parent = getOrCreate(clazz.superName);

        }
        if (!clazz.interfaces.isEmpty()) {
            node.interfaces = clazz.interfaces.stream().map(this::getOrCreate).collect(Collectors.toList());
        }
    }

    private ApkClassInfo getOrCreate(String name) {
        return classes.computeIfAbsent(name, ApkClassInfo::createStub);
    }


    @Nullable
    @Override
    public ClassInfo get(String name) {
        return classes.get(name);
    }
}
