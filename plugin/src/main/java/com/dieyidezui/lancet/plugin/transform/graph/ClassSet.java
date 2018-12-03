package com.dieyidezui.lancet.plugin.transform.graph;

import com.android.build.api.transform.Status;
import com.dieyidezui.lancet.plugin.bean.ClassInfo;
import com.dieyidezui.lancet.plugin.util.Constants;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ClassSet {

    private Map<String, ClassNode> classes = new ConcurrentHashMap<>(Constants.OPT_SIZE);

    public Consumer<List<ClassInfo>> asConsumer() {
        return list -> list.parallelStream()
                .forEach(c -> add(c, Status.NOTCHANGED));
    }

    public Supplier<List<ClassInfo>> asSupplier() {
        return () -> classes.values().stream()
                .filter(n -> !n.isVirtual())
                .map(n -> n.clazz)
                .collect(Collectors.toCollection(() -> new ArrayList<>(classes.size())));
    }

    /**
     * Don't generate the reverse direction edge because of concurrent problem, do it later.
     *
     * @param clazz the class info
     * @param status the class status
     */
    public void add(ClassInfo clazz, Status status) {
        ClassNode node = getOrCreate(clazz.name);
        node.status = status;
        node.clazz = clazz;
        if (clazz.superName != null) {
            node.parent = getOrCreate(clazz.superName);

        }
        if (!clazz.interfaces.isEmpty()) {
            node.interfaces = clazz.interfaces.stream().map(this::getOrCreate).collect(Collectors.toList());
        }
    }

    public void removed(String className) {
        getOrCreate(className).removed();
    }

    @Nullable
    public ClassNode get(String className) {
        return classes.get(className);
    }

    private ClassNode getOrCreate(String name) {
        return classes.computeIfAbsent(name, ClassNode::createStub);
    }

}
