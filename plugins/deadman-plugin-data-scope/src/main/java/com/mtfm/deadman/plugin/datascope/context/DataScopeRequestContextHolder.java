package com.mtfm.deadman.plugin.datascope.context;

import com.mtfm.deadman.plugin.datascope.model.DataScopeUserContext;

/**
 * 当前 HTTP 请求的数据权限用户上下文（由插件 Filter 从缓存注入）。
 */
public final class DataScopeRequestContextHolder {

    private static final ThreadLocal<DataScopeUserContext> CONTEXT = new ThreadLocal<>();

    private DataScopeRequestContextHolder() {
    }

    /**
     * 设置当前请求的数据权限上下文。
     *
     * @param context 用户数据范围上下文
     */
    public static void set(DataScopeUserContext context) {
        CONTEXT.set(context);
    }

    /**
     * 获取当前请求的数据权限上下文。
     *
     * @return 上下文，未设置时返回 null
     */
    public static DataScopeUserContext get() {
        return CONTEXT.get();
    }

    /**
     * 清除当前线程上下文。
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
