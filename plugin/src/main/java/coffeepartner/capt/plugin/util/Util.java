package coffeepartner.capt.plugin.util;

import com.android.build.api.transform.TransformException;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Util {

    public static <T> T await(Future<T> future) throws IOException, TransformException, InterruptedException {
        try {
            return future.get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            }
            throw new TransformException(e.getCause());
        }
    }

    // Ljava/lang/Object; to java/lang/Object
    public static String objDescToInternalName(String desc) {
        return desc.substring(1, desc.length() - 1);
    }

    public static ClassNode clone(ClassNode node) {
        ClassNode cloned = new ClassNode();
        node.accept(cloned);
        return cloned;
    }
}
