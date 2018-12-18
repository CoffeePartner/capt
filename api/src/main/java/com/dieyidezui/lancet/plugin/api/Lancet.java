package com.dieyidezui.lancet.plugin.api;

import com.dieyidezui.lancet.plugin.api.graph.ClassGraph;
import com.dieyidezui.lancet.plugin.api.process.MetaProcessor;
import com.dieyidezui.lancet.plugin.api.transform.ClassTransformer;
import org.gradle.internal.HasInternalProtocol;

import java.lang.annotation.Annotation;

@HasInternalProtocol
public interface Lancet {

    boolean isIncremental();

    Context getContext();

    ClassGraph classGraph();

    Arguments getArgs();

    void registerMetaProcessor(MetaProcessor processor, Class<? extends Annotation>... interestedIn);

    void registerClassTransformer(ClassTransformer transformer);

    OutputProvider outputs();
}
