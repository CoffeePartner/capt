package com.dieyidezui.lancet.plugin.api.graph;

import javax.annotation.Nullable;

public interface ClassGraph {

    @Nullable
    ClassInfo get(String name);
}
