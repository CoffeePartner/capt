package com.dieyidezui.lancet.plugin.api;

import com.dieyidezui.lancet.plugin.api.graph.ClassGraph;
import org.gradle.internal.HasInternalProtocol;


@HasInternalProtocol
public interface Lancet {

    boolean isIncremental();

    Context getContext();

    ClassGraph classGraph();

    Arguments getArgs();

    OutputProvider outputs();
}
