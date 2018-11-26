package com.dieyidezui.lancet.plugin.bean.hook;

import com.dieyidezui.lancet.rt.annotations.Insert;
import me.ele.lancet.weaver.internal.meta.HookInfoLocator;

import javax.annotation.Nullable;
import java.util.List;

public class GeneratedInterceptorInfo {

    public String bindClass;

    public String bindMethod;

    /**
     * Just for {@link Insert}
     */
    @Nullable
    public String associatedMethod;

    /**
     * Just for {@link Insert}
     */
    @Nullable
    public String virtualMethod;

    public String generatedClass;

    public List<MethodLocator> AffectedBy;
}
