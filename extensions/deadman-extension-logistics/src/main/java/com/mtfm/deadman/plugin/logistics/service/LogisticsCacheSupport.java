package com.mtfm.deadman.plugin.logistics.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.mtfm.deadman.plugin.logistics.config.LogisticsPluginProperties;
import com.mtfm.deadman.plugin.logistics.constant.LogisticsCacheNames;
import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarrierDetectResult;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryResult;

/**
 * 物流 Redis 缓存读写辅助。
 */
@Service
public class LogisticsCacheSupport {

    private final LogisticsPluginProperties properties;
    private final CacheManager cacheManager;

    /**
     * 构造物流缓存辅助。
     *
     * @param properties   物流插件配置
     * @param cacheManager Spring Cache 管理器，测试环境可为 null
     */
    public LogisticsCacheSupport(
            LogisticsPluginProperties properties, @Autowired(required = false) CacheManager cacheManager) {
        this.properties = properties;
        this.cacheManager = cacheManager;
    }

    /**
     * 缓存是否启用。
     *
     * @return true 表示启用
     */
    public boolean isCacheEnabled() {
        return properties.getCache().isEnabled();
    }

    /**
     * 构建轨迹查询缓存键。
     *
     * @param providerId Provider 标识
     * @param contextKey 查单参数键
     * @return 缓存键
     */
    public String trackQueryCacheKey(String providerId, String contextKey) {
        return providerId + ":" + contextKey;
    }

    /**
     * 构建查单参数键。
     *
     * @param carrierCode 快递公司编码
     * @param trackingNo  快递单号
     * @param phone       手机号后四位
     * @return 参数键
     */
    public String trackQueryContextKey(String carrierCode, String trackingNo, String phone) {
        String normalizedPhone = phone == null ? "" : phone.trim();
        return carrierCode + ":" + trackingNo.trim() + ":" + normalizedPhone;
    }

    /**
     * 构建快递公司识别缓存键。
     *
     * @param providerId Provider 标识
     * @param trackingNo 快递单号
     * @return 缓存键
     */
    public String carrierDetectCacheKey(String providerId, String trackingNo) {
        return providerId + ":" + trackingNo.trim();
    }

    /**
     * 读取轨迹查询缓存。
     *
     * @param cacheKey 缓存键
     * @return 缓存值
     */
    public Optional<LogisticsTrackQueryResult> getTrackQuery(String cacheKey) {
        return get(cacheKey, LogisticsCacheNames.TRACK_QUERY, LogisticsTrackQueryResult.class);
    }

    /**
     * 写入轨迹查询缓存。
     *
     * @param cacheKey 缓存键
     * @param value    缓存值
     */
    public void putTrackQuery(String cacheKey, LogisticsTrackQueryResult value) {
        put(cacheKey, LogisticsCacheNames.TRACK_QUERY, value);
    }

    /**
     * 读取快递公司识别缓存。
     *
     * @param cacheKey 缓存键
     * @return 缓存值
     */
    @SuppressWarnings("unchecked")
    public Optional<List<LogisticsCarrierDetectResult>> getCarrierDetect(String cacheKey) {
        if (!isCacheEnabled()) {
            return Optional.empty();
        }
        Cache cache = resolveCache(LogisticsCacheNames.CARRIER_DETECT);
        if (cache == null) {
            return Optional.empty();
        }
        Cache.ValueWrapper wrapper = cache.get(cacheKey);
        if (wrapper == null || wrapper.get() == null) {
            return Optional.empty();
        }
        return Optional.of((List<LogisticsCarrierDetectResult>) wrapper.get());
    }

    /**
     * 写入快递公司识别缓存。
     *
     * @param cacheKey 缓存键
     * @param value    缓存值
     */
    public void putCarrierDetect(String cacheKey, List<LogisticsCarrierDetectResult> value) {
        put(cacheKey, LogisticsCacheNames.CARRIER_DETECT, value);
    }

    private <T> Optional<T> get(String cacheKey, String cacheName, Class<T> type) {
        if (!isCacheEnabled()) {
            return Optional.empty();
        }
        Cache cache = resolveCache(cacheName);
        if (cache == null) {
            return Optional.empty();
        }
        Cache.ValueWrapper wrapper = cache.get(cacheKey);
        if (wrapper == null || wrapper.get() == null) {
            return Optional.empty();
        }
        return Optional.of(type.cast(wrapper.get()));
    }

    private void put(String cacheKey, String cacheName, Object value) {
        if (!isCacheEnabled() || value == null) {
            return;
        }
        Cache cache = resolveCache(cacheName);
        if (cache != null) {
            cache.put(cacheKey, value);
        }
    }

    private Cache resolveCache(String cacheName) {
        if (cacheManager == null) {
            return null;
        }
        return cacheManager.getCache(cacheName);
    }
}
