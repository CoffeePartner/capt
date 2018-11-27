package stub.weaver.internal.meta;

import stub.weaver.internal.parser.AnnotationMeta;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

/**
 * Created by gengwanpeng on 17/5/3.
 */
public class MethodMetaInfo {

    public MethodNode sourceNode;
    public List<AnnotationMeta> metaList;

    public MethodMetaInfo(MethodNode sourceNode) {
        this.sourceNode = sourceNode;
    }
}
