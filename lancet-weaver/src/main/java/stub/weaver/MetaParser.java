package stub.weaver;

import java.util.List;

import stub.weaver.internal.entity.TransformInfo;
import stub.weaver.internal.graph.Graph;


/**
 *
 * Created by gengwanpeng on 17/3/21.
 */
public interface MetaParser {

    TransformInfo parse(List<String> classes, Graph graph);
}
