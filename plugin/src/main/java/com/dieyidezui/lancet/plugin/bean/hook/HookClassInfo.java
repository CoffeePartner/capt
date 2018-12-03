package com.dieyidezui.lancet.plugin.bean.hook;

import javafx.util.Pair;

import java.util.List;

public class HookClassInfo {

    public String className;

    public String belongToInputName;

    public TryCatchInfo tryCatchInfos;

    /**
     * info with extra matched classes
     */
    public List<Pair<InsertInfo, List<String>>> insertInfos;

    /**
     * same as insert
     */
    public List<Pair<ProxyInfo, List<String>>> proxyInfos;
}
