package coffeepartner.capt.plugin.graph;

import coffeepartner.capt.plugin.api.graph.MethodInfo;

import javax.annotation.Nullable;

/**
 * Ignore exceptions, too large
 */
public class MethodBean implements MethodInfo {

    public int access;
    public String name;
    public String desc;
    @Nullable
    public String signature;

    public MethodBean(int access, String name, String desc, @Nullable String signature) {
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.signature = signature;
    }

    @Override
    public int access() {
        return access;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String desc() {
        return desc;
    }

    @Nullable
    @Override
    public String signature() {
        return signature;
    }

    @Override
    public String toString() {
        return "MethodBean{" +
                "access=" + access +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
