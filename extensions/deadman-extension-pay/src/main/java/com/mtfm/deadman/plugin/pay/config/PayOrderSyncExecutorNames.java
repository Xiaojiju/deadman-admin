package com.mtfm.deadman.plugin.pay.config;

/**
 * 支付单主动查单线程池 Bean 名称常量。
 */
public final class PayOrderSyncExecutorNames {

    /**
     * 插件默认可注入的查单线程池 Bean 名称。
     * 宿主也可通过 {@code deadman.plugin.pay.sync.executor-bean-name} 指定应用全局线程池。
     */
    public static final String EXECUTOR_BEAN_NAME = "payOrderSyncExecutor";

    /**
     * 支付模块异步任务线程池（异常退款 AFTER_COMMIT 处理等）。
     */
    public static final String ASYNC_EXECUTOR_BEAN_NAME = "payAsyncExecutor";

    private PayOrderSyncExecutorNames() {
    }
}
