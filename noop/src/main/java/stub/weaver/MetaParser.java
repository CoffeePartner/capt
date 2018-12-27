package stub.weaver;

import stub.weaver.internal.entity.TransformInfo;
import stub.weaver.internal.graph.Graph;

import java.util.List;


/**
 * Created by gengwanpeng on 17/3/21.
 */
public interface MetaParser {

    TransformInfo parse(List<String> classes, Graph graph);
}
