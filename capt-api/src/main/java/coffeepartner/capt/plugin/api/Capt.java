package coffeepartner.capt.plugin.api;

import coffeepartner.capt.plugin.api.graph.ClassGraph;
import org.gradle.internal.HasInternalProtocol;


@HasInternalProtocol
public interface Capt {
    /**
     * @return true if this build is incremental
     */
    boolean isIncremental();

    /**
     * @return context cpat passed
     */
    Context getContext();

    /**
     * @return class graph in apk
     */
    ClassGraph getClassGraph();

    /**
     * @return arguments in build.gradle
     */
    Arguments getArgs();

    /**
     * @return output provider to store cache
     */
    OutputProvider getOutputs();
}
