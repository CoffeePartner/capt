package stub.weaver.internal.parser.anno;

import stub.weaver.internal.log.Log;
import stub.weaver.internal.meta.HookInfoLocator;
import stub.weaver.internal.parser.AcceptableAnnoParser;
import stub.weaver.internal.parser.AnnotationMeta;
import org.objectweb.asm.tree.AnnotationNode;

/**
 * Created by gengwanpeng on 17/5/4.
 */
public class AcceptAny implements AcceptableAnnoParser {

    public static AcceptAny INSTANCE = new AcceptAny();

    private AcceptAny(){
    }

    @Override
    public boolean accept(String desc) {
        return true;
    }

    @Override
    public AnnotationMeta parseAnno(AnnotationNode annotationNode) {
        Log.w("Annotation " + annotationNode.desc + " is lost.");
        return new AnnotationMeta(annotationNode.desc) {

            @Override
            public void accept(HookInfoLocator locator) {
            }
        };
    }
}
