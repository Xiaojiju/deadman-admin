package com.mtfm.deadman.plugin.pay.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 支付单主动查单内置定时任务自动配置。
 * 关闭 {@code deadman.plugin.pay.sync.scheduler-enabled} 后不会注册 Spring 调度，业务服务仍可用。
 */
@AutoConfiguration
@EnableScheduling
@ConditionalOnProperty(prefix = "deadman.plugin.pay.sync", name = "scheduler-enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.mtfm.deadman.plugin.pay.scheduler")
public class PaySyncSchedulerAutoConfiguration {
}
