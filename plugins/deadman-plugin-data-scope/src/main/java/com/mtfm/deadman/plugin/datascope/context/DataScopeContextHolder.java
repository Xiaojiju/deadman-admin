package com.mtfm.deadman.plugin.datascope.context;

/**
 * 数据隔离调用链上下文：{@link com.mtfm.deadman.plugin.datascope.annotation.DataScope} 启用标记、
 * {@link com.mtfm.deadman.plugin.datascope.annotation.DataScopeIgnore} 忽略标记。
 */
public final class DataScopeContextHolder {

    private static final ThreadLocal<Boolean> ENABLED = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final ThreadLocal<Boolean> IGNORED = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private DataScopeContextHolder() {
    }

    /**
     * 启用当前线程的数据隔离（由 {@code @DataScope} 切面调用）。
     */
    public static void enable() {
        ENABLED.set(Boolean.TRUE);
    }

    /**
     * 当前线程是否处于 {@code @DataScope} 调用链内。
     *
     * @return 已启用时返回 true
     */
    public static boolean isEnabled() {
        return Boolean.TRUE.equals(ENABLED.get());
    }

    /**
     * 清除当前线程启用标记。
     */
    public static void clearEnabled() {
        ENABLED.remove();
    }

    /**
     * 忽略当前线程的数据隔离（由 {@code @DataScopeIgnore} 切面调用）。
     */
    public static void ignore() {
        IGNORED.set(Boolean.TRUE);
    }

    /**
     * 当前线程是否忽略数据隔离。
     *
     * @return 忽略时返回 true
     */
    public static boolean isIgnored() {
        return Boolean.TRUE.equals(IGNORED.get());
    }

    /**
     * 清除当前线程忽略标记。
     */
    public static void clearIgnored() {
        IGNORED.remove();
    }
}
