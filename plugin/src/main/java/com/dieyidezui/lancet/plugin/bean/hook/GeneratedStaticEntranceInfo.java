package com.dieyidezui.lancet.plugin.bean.hook;

import java.util.List;

/**
 * GeneratedStaticEntrance 只会被 Hook 方法存在与否影响，目前只有 TryCatch Proxy 会路由至此
 */
public class GeneratedStaticEntranceInfo {

    public MethodLocator locator;

    public boolean targetStatic;

    public List<Integer> requireInterceptors;
}
