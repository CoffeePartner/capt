package com.dieyidezui.lancet.plugin.api.graph;

import com.dieyidezui.lancet.plugin.api.Status;

import javax.annotation.Nullable;
import java.util.List;

public interface ClassInfo {

    /**
     * @return status from last build
     */
    Status status();

    /**
     * class access with deprecated flag, {@see Opcodes.ACC_DEPRECATED}
     *
     * @return class access.
     */
    int access();

    /**
     * @return class name.
     */
    String name();

    /**
     * Use it carefully! This class MUST NOT use classes in Android Framework.
     *
     * @return class of this.
     */
    Class<?> loadClass();

    /**
     * @return the signature of this class. May be {@literal null} if the class is not a
     * generic one, and does not extend or implement generic classes or interfaces.
     */
    @Nullable
    String signature();

    /**
     * Returns null if this class is not exists in apk.
     *
     * @return super class
     */
    @Nullable
    ClassInfo parent();

    /**
     * If this is a interface, returns this extends interfaces.
     * Otherwise, returns this implements interfaces.
     *
     * @return interfaces.
     */
    List<ClassInfo> interfaces();

    /**
     * If this is not a interface, returns classes extends this.
     * Otherwise, returns empty list.
     *
     * @return classes extends this.
     */
    List<ClassInfo> classChildren();

    /**
     * If this is interface, returns interfaces extends this.
     * Otherwise, returns empty list.
     *
     * @return interfaces extends this.
     */
    List<ClassInfo> interfaceChildren();

    /**
     * If this is interface, returns classes implements this "directly".
     * Directly means literal implements on class definition.
     *
     * @return classes implements this.
     */
    List<ClassInfo> implementedClasses();
}
