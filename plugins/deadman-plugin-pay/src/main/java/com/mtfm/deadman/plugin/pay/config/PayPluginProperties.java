package com.mtfm.deadman.plugin.pay.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import lombok.Data;

/**
 * 支付插件配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.pay")
public class PayPluginProperties {

    /** 是否启用插件 */
    private boolean enabled = true;

    /** 默认支付 Provider 标识，如 wechat-jsapi */
    private String defaultProvider = "wechat-jsapi";

    /** 待支付单主动查单配置 */
    @NestedConfigurationProperty
    private Sync sync = new Sync();

    /**
     * 待支付单主动查单配置。
     */
    @Data
    public static class Sync {

        /** 是否启用内置 Spring 定时查单任务；关闭后可接入 XXL-Job 等外部调度调用 {@code PaymentOrderSyncService} */
        private boolean schedulerEnabled = true;

        /** 内置定时任务 cron 表达式 */
        private String cron = "0 * * * * ?";

        /** 预下单后至少等待多久再查单，避免刚下单即查 */
        private Duration minAge = Duration.ofMinutes(2);

        /** 超过该时间的待支付单不再主动查单 */
        private Duration maxAge = Duration.ofMinutes(30);

        /** 单次扫描最多处理的待支付单数量 */
        private int batchSize = 50;
    }
}
