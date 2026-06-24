package com.mtfm.deadman.plugin.logistics.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 物流能力延伸配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.logistics")
public class LogisticsPluginProperties {

    /** 是否启用物流能力 */
    private boolean enabled = true;

    /** 默认物流 Provider 标识 */
    private String defaultProvider = "kuaidi100";

    /** 缓存配置 */
    private Cache cache = new Cache();

    /**
     * 物流 Redis 缓存配置。
     */
    @Data
    public static class Cache {

        /** 是否启用 Redis 轨迹/识别缓存 */
        private boolean enabled = true;

        /** 轨迹查询缓存 TTL，短 TTL 降低渠道调用频率 */
        private Duration trackQueryTtl = Duration.ofMinutes(5);

        /** 快递公司识别缓存 TTL */
        private Duration carrierDetectTtl = Duration.ofMinutes(30);
    }
}
