package bean;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClassInfo {

    public int access;
    public String name;
    @Nullable
    public String superName;
    public List<String> interfaces;

    // RemoveWhenTransform field info because useless till now.
    //public List<FieldInfo> fields = new ArrayList<>();

    public List<MethodInfo> methods = new ArrayList<>();


    public ClassInfo(String name) {
        this(0, name, null);
    }


    public ClassInfo(int access, String name, @Nullable String superName) {
        this.access = access;
        this.name = name;
        this.superName = superName;
        this.interfaces = Collections.emptyList();
        this.methods = Collections.emptyList();
    }

    public void addMethod(MethodInfo methodInfo) {
        if (methods == Collections.<MethodInfo>emptyList()) {
            methods = new ArrayList<>();
        }
        methods.add(methodInfo);
    }
}
