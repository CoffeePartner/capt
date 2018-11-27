package com.dieyidezui.lancet.plugin.bean.hook;


import com.dieyidezui.lancet.rt.annotations.Insert;

import javax.annotation.Nullable;
import java.util.List;

/**
 * GeneratedInterface 有两种：
 * 1. 每个 hook 方法对应 一个，这种只会被单向影响，且目标方法只有一个
 * 2. 每个 Insert & Proxy 匹配到的 Class 对应一个(存在 synthetic associated 方法)，这种可能会被双向影响。
 *   1. 一方面是目标类的方法 存在与否
 *   2. 一方面是Hook的方法存在与否
 */
public class GeneratedInterceptorInfo {

    /**
     * 对于 1：为 hook 方法
     * 对于 2：为 被 hook 方法
     * 无论类型，只要 binded 目标不存在，则应当删除
     */
    public MethodLocator binded;

    /**
     * 非 null，则为 {@link Insert}，则需要将 associated 方法还原至原方法
     */
    @Nullable
    public String associatedMethod;

    /**
     * 非 null 则为类型2
     */
    @Nullable
    public String syntheticMethod;

    /**
     * 生成的 class 内部名称，类型 1 则伴 随 hook 类，类型 2 则伴随被 hook 类
     */
    public String generatedClass;

    /**
     * null 表示类型 1
     * 非 null 表示 类型 2, empty() 则需要移除
     */
    @Nullable
    public List<MethodLocator> sources;
}
