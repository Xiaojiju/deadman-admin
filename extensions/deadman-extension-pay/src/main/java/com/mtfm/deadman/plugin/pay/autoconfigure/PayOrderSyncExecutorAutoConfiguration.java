package com.mtfm.deadman.plugin.pay.autoconfigure;

import java.util.concurrent.Executor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.mtfm.deadman.plugin.pay.config.PayOrderSyncExecutorNames;
import com.mtfm.deadman.plugin.pay.config.PayPluginProperties;

/**
 * 支付单主动查单默认线程池自动配置。
 * 仅在未配置 {@code executor-bean-name}、未声明
 * {@link PayOrderSyncExecutorNames#EXECUTOR_BEAN_NAME} Bean 时注册自建池。
 */
@AutoConfiguration
@EnableConfigurationProperties(PayPluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.pay", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PayOrderSyncExecutorAutoConfiguration {

    /**
     * 注册默认主动查单线程池（兜底：无配置、无注入时使用）。
     *
     * @param payPluginProperties 支付插件配置
     * @return 查单并行执行器
     */
    @Bean(name = PayOrderSyncExecutorNames.EXECUTOR_BEAN_NAME)
    @ConditionalOnMissingBean(name = PayOrderSyncExecutorNames.EXECUTOR_BEAN_NAME)
    @ConditionalOnProperty(prefix = "deadman.plugin.pay.sync", name = "parallel-enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnExpression("'${deadman.plugin.pay.sync.executor-bean-name:}'.trim().isEmpty()")
    public Executor payOrderSyncExecutor(PayPluginProperties payPluginProperties) {
        PayPluginProperties.Sync sync = payPluginProperties.getSync();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int parallelism = Math.max(sync.getParallelism(), 1);
        executor.setCorePoolSize(parallelism);
        executor.setMaxPoolSize(parallelism);
        executor.setQueueCapacity(Math.max(sync.getBatchSize(), parallelism) * 2);
        executor.setThreadNamePrefix("pay-order-sync-");
        executor.initialize();
        return executor;
    }
}
