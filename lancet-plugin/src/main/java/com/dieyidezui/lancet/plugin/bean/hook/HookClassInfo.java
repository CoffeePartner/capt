package com.dieyidezui.lancet.plugin.bean.hook;

import java.util.List;

public class HookClassInfo {

    public String className;

    public String belongToInputName;

    public List<TryCatchInfo> tryCatchInfos;

    public List<InsertInfo> insertInfos;

    public List<ProxyInfo> proxyInfos;
}
