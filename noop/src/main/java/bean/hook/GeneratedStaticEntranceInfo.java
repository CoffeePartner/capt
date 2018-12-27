package bean.hook;

import java.util.List;

/**
 * GeneratedStaticEntrance 只会被 Hook 方法存在与否影响，目前只有 TryCatch Proxy 会路由至此
 */
public class GeneratedStaticEntranceInfo {

    /**
     * Proxy Desc like:
     * 1. none static: target args this
     * 2. static: args this
     * Throw Desc like:
     * 1. (type, this)type
     */
    public MethodLocator locator;

    /**
     * 因为自身是静态方法，所以通过 desc 无法区分目标是是否静态
     */
    public boolean targetStatic;

    /**
     * GeneratedInterceptorInfo 在列表中的索引
     */
    public List<Integer> requireInterceptors;
}
