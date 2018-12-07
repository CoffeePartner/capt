package com.dieyidezui.lancet.rt.compiletime;

import com.dieyidezui.lancet.rt.internal.annotations.AutoRemovedAfterCompile;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@AutoRemovedAfterCompile
public class ClassInfo {

    private final int access;
    private final String name;
    private final String signature;
    private final String superName;
    private final List<String> interfaces;

    public ClassInfo(int access, String name, String signature, String superName, String[] interfaces) {
        this.access = access;
        this.name = name;
        this.signature = signature;
        this.superName = superName;
        this.interfaces = interfaces == null ? Collections.<String>emptyList() : Arrays.asList(interfaces);
    }

    /**
     * @return the class's access flags (see {@link Modifier}). This parameter also indicates if the class is deprecated.
     * ACC_DEPRECATED = 0x20000;
     */
    public int getAccess() {
        return access;
    }

    public String getName() {
        return name;
    }

    /**
     * @return the signature of this class. May be {@literal null} if the class is not a
     * generic one, and does not extend or implement generic classes or interfaces.
     */
    @Nullable
    public String getSignature() {
        return signature;
    }

    public String getSuperName() {
        return superName;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }
}
