package graph;

import bean.ClassInfo;
import com.android.build.api.transform.Status;
import com.dieyidezui.lancet.rt.Scope;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ClassNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassNode.class);

    /**
     * null means not exists in APK code
     * {@code REMOVED} means it removed this build, but last build exists
     */
    @Nullable
    public Status status;
    public ClassInfo clazz;

    public static ClassNode createStub(String name) {
        return new ClassNode(new ClassInfo(name));
    }

    private ClassNode(ClassInfo clazz) {
        this.clazz = clazz;
    }

    /**
     * null means removed or is {@link Object}
     */
    @Nullable
    public ClassNode parent;
    public List<ClassNode> interfaces = Collections.emptyList();

    // rev direction
    public List<ClassNode> classChildren = Collections.emptyList();
    public List<ClassNode> interfaceChildren = Collections.emptyList();
    public List<ClassNode> implementedClasses = Collections.emptyList();

    void removed() {
        status = Status.REMOVED;
        parent = null;
        interfaces = classChildren = interfaceChildren = implementedClasses = Collections.emptyList();
    }

    public boolean isVirtual() {
        return status != null && status != Status.REMOVED;
    }

    public boolean isInterface() {
        return (clazz.access & Opcodes.ACC_INTERFACE) != 0;
    }

    public void visitClasses(Scope scope, Consumer<ClassNode> visitor) {
        switch (scope) {
            case SELF:
                visitor.accept(this);
                break;
            case ALL:
                classChildren.forEach(n -> n.visitClasses(scope, visitor));
            case DIRECT:
                classChildren.forEach(visitor);
                break;
            case LEAF:
                classChildren.stream()
                        .filter(n -> {
                            if (n.classChildren.isEmpty()) {
                                visitor.accept(n);
                                return false;
                            }
                            return true;
                        })
                        .forEach(n -> n.visitClasses(scope, visitor));
                break;
        }
    }

    public void visitImplements(Scope scope, Consumer<ClassNode> visitor) {
        switch (scope) {
            case ALL:
                implementedClasses.forEach(n -> n.visitClasses(scope, visitor));
            case DIRECT:
                interfaceChildren.forEach(n -> n.visitImplements(scope, visitor));
            case SELF:
                implementedClasses.forEach(visitor);
                break;
            case LEAF:
                interfaceChildren.forEach(n -> n.visitImplements(scope, visitor));
                implementedClasses.stream()
                        .filter(n -> {
                            if (n.classChildren.isEmpty()) {
                                visitor.accept(n);
                                return false;
                            }
                            return true;
                        })
                        .forEach(n -> visitClasses(scope, visitor));
        }
    }
}
