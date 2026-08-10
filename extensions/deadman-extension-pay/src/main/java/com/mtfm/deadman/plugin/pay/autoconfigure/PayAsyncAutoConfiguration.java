package com.mtfm.deadman.plugin.pay.autoconfigure;

import java.util.concurrent.Executor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import com.mtfm.deadman.plugin.pay.config.PayOrderSyncExecutorNames;

/**
 * 支付模块异步执行配置（异常退款 AFTER_COMMIT 等）。
 */
@AutoConfiguration
@EnableAsync
@ConditionalOnProperty(prefix = "deadman.plugin.pay", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PayAsyncAutoConfiguration {

    /**
     * 注册支付异步任务执行器（虚拟线程，避免阻塞平台线程）。
     *
     * @return 异步执行器
     */
    @Bean(name = PayOrderSyncExecutorNames.ASYNC_EXECUTOR_BEAN_NAME)
    @ConditionalOnMissingBean(name = PayOrderSyncExecutorNames.ASYNC_EXECUTOR_BEAN_NAME)
    public Executor payAsyncExecutor() {
        return runnable -> Thread.ofVirtual()
                .name("pay-async-", 0)
                .start(runnable);
    }
}
