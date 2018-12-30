package coffeepartner.capt.plugin.api.graph;


import javax.annotation.Nullable;

public interface MethodInfo {

    /**
     * method access with deprecated flag, see Opcodes.ACC_DEPRECATED
     *
     * @return method access
     */
    int access();

    /**
     * @return method name
     */
    String name();

    /**
     * @return method desc
     */
    String desc();

    /**
     * @return the method's signature. May be {@literal null} if the method parameters,
     * return type and exceptions do not use generic types.
     */
    @Nullable
    String signature();
}
