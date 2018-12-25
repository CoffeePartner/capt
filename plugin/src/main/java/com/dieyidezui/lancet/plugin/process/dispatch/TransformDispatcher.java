package com.dieyidezui.lancet.plugin.process.dispatch;

import com.android.build.api.transform.TransformInvocation;
import com.dieyidezui.lancet.plugin.api.process.MetaProcessor;
import com.dieyidezui.lancet.plugin.api.transform.ClassTransformer;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransformDispatcher {


    private final TransformInvocation invocation;
    private final GlobalResource global;
    private boolean incremental = false;
    private Set<String> rerack = Collections.emptySet();

    public TransformDispatcher(TransformInvocation invocation, GlobalResource global) {
        this.invocation = invocation;
        this.global = global;
    }

    public void dispatchMetas(Map<String, MetaProcessor> processors) {

    }

    public void dispatchTransforms(List<ClassTransformer> transformers) {

    }

    public void rerack(Set<String> classes) {
        incremental = true;
        rerack = classes;
    }
}
