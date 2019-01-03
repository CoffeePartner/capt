package coffeepartner.capt.plugin.api.graph;

import javax.annotation.Nullable;

public interface ClassGraph {

    /**
     *
     * @param name class name
     * @return class info
     */
    @Nullable
    ClassInfo get(String name);
}
