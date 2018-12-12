package com.dieyidezui.lancet.plugin.api;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Map;

public interface Lancet {

    boolean incremental();

    Arguments getArgs();

    void registerMetaProcessor(MetaProcessor processor, Class<? extends Annotation>... interestedIn);

    void addEventCallback();

    void registerClassTransformer(ClassTransformer transformer);
}
