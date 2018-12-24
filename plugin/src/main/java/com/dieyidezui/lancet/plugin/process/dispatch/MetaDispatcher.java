package com.dieyidezui.lancet.plugin.process.dispatch;

import com.android.build.api.transform.TransformInvocation;
import com.dieyidezui.lancet.plugin.api.graph.ClassGraph;
import com.dieyidezui.lancet.plugin.api.process.MetaProcessor;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;

import java.util.List;
import java.util.Set;

public class MetaDispatcher {

    private final GlobalResource global;

    public MetaDispatcher(GlobalResource global, ClassGraph graph) {
        this.global = global;
    }

    public void dispatchTo(MetaProsessorFactory factory) {

    }

    public interface MetaProsessorFactory {

        List<MetaProcessor> support(Set<String> annotation);
    }
}
