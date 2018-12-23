package com.dieyidezui.lancet.plugin.process.dispatch;

import com.android.build.api.transform.TransformInvocation;
import com.dieyidezui.lancet.plugin.api.process.MetaProcessor;
import com.dieyidezui.lancet.plugin.api.transform.ClassTransformer;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;

import java.util.List;
import java.util.Map;

public class ClassDispatcher {


    public ClassDispatcher(TransformInvocation invocation, GlobalResource global) {
    }

    public void onPrePlugins() {

    }

    public void onCurPlugins() {

    }

    public void dispatchMetas(Map<String, MetaProcessor> processors) {

    }

    public void dispatchTransforms(List<ClassTransformer> transformers) {

    }

    public void rerack(List<String> classes) {
    }
}
