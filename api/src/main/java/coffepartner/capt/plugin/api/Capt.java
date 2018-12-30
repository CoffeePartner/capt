package coffeepartner.capt.plugin.api;

import coffeepartner.capt.plugin.api.graph.ClassGraph;
import org.gradle.internal.HasInternalProtocol;


@HasInternalProtocol
public interface Capt {

    boolean isIncremental();

    Context getContext();

    ClassGraph classGraph();

    Arguments getArgs();

    OutputProvider outputs();
}
