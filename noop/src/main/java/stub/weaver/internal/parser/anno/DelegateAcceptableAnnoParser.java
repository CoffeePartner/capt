package stub.weaver.internal.parser.anno;

import org.objectweb.asm.tree.AnnotationNode;
import stub.weaver.internal.parser.AcceptableAnnoParser;
import stub.weaver.internal.parser.AnnoParser;
import stub.weaver.internal.parser.AnnotationMeta;

/**
 * Created by gengwanpeng on 17/5/3.
 */
public class DelegateAcceptableAnnoParser implements AcceptableAnnoParser {

    private final String desc;
    private final AnnoParser parser;

    public DelegateAcceptableAnnoParser(String desc, AnnoParser parser) {

        this.desc = desc;
        this.parser = parser;
    }

    @Override
    public boolean accept(String desc) {
        return this.desc.equals(desc);
    }

    @Override
    public AnnotationMeta parseAnno(AnnotationNode annotationNode) {
        return parser.parseAnno(annotationNode);
    }
}
