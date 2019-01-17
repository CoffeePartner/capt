package coffeepartner.capt.plugin.api.graph;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * REMOVED and NOT_EXISTS are not in graph.
 */
public interface ClassInfo {

    /**
     * @return status from last build
     */
    Status status();

    /**
     * Alias for: status != REMOVED and status != NOT_EXISTS.
     *
     * @return true if exists in APK.
     */
    boolean exists();

    /**
     * class access with deprecated flag, see Opcodes.ACC_DEPRECATED.
     *
     * @return class access.
     */
    int access();

    /**
     * @return class name.
     */
    String name();

    /**
     * Use it carefully! This class MUST NOT depend on classes in Android Framework.
     *
     * @return class of this.
     * @throws ClassNotFoundException not found class
     */
    Class<?> loadClass() throws ClassNotFoundException;


    /**
     * if {@link #exists()} returns false, it will open failed.
     *
     * @return the input stream of class bytes
     * @throws IOException if open failed
     */
    InputStream openStream() throws IOException;

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
     * @return The methods it own.
     */
    List<? extends MethodInfo> methods();

    /**
     * If this is a interface, returns this extends interfaces.
     * Otherwise, returns this implements interfaces.
     *
     * @return interfaces.
     */
    List<? extends ClassInfo> interfaces();

    /**
     * If this is not a interface, returns classes extends this.
     * Otherwise, returns empty list.
     *
     * @return classes extends this.
     */
    List<? extends ClassInfo> classChildren();

    /**
     * If this is interface, returns interfaces extends this.
     * Otherwise, returns empty list.
     *
     * @return interfaces extends this.
     */
    List<? extends ClassInfo> interfaceChildren();

    /**
     * If this is interface, returns classes implements this "directly".
     * Directly means literal implements on class definition.
     *
     * @return classes implements this.
     */
    List<? extends ClassInfo> implementedClasses();

}
