package com.dieyidezui.lancet.plugin.bean.hook;


import javax.annotation.Nullable;

public class InsertInfo {

    @Nullable
    public TargetClassInfo targetClass;

    @Nullable
    public ImplementedInterfaceInfo interfaces;

    public String targetMethod;

    public boolean createSuper;

    public int priority;

    public MethodLocator locator;
}
