package com.mtfm.deadman.plugin.logistics.config;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.common.spi.CacheTtlContributor;
import com.mtfm.deadman.common.spi.NamedCacheTtl;
import com.mtfm.deadman.plugin.logistics.constant.LogisticsCacheNames;

/**
 * 物流模块 Redis 缓存 TTL 贡献者。
 */
@Component
@ConditionalOnProperty(prefix = "deadman.plugin.logistics.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LogisticsCacheTtlContributor implements CacheTtlContributor {

    private final LogisticsPluginProperties properties;

    /**
     * 构造物流缓存 TTL 贡献者。
     *
     * @param properties 物流插件配置
     */
    public LogisticsCacheTtlContributor(LogisticsPluginProperties properties) {
        this.properties = properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NamedCacheTtl> contribute() {
        return List.of(
                new NamedCacheTtl(
                        LogisticsCacheNames.TRACK_QUERY, properties.getCache().getTrackQueryTtl()),
                new NamedCacheTtl(
                        LogisticsCacheNames.CARRIER_DETECT, properties.getCache().getCarrierDetectTtl()));
    }
}
