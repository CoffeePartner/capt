package com.dieyidezui.lancet.plugin.bean.hook;

import javax.annotation.Nullable;

public class ProxyInfo {

    @Nullable
    public TargetClassInfo targetClass;

    @Nullable
    public ImplementedInterfaceInfo interfaces;


    @Nullable
    public String regex;

    public String targetMethod;

    public int priority;

    public MethodLocator locator;
}
