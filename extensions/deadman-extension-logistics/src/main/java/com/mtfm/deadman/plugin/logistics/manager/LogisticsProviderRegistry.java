package com.mtfm.deadman.plugin.logistics.manager;

import java.util.List;

import org.springframework.stereotype.Component;

import com.mtfm.deadman.plugin.logistics.config.LogisticsPluginProperties;
import com.mtfm.deadman.plugin.logistics.spi.carrier.LogisticsCarrierProvider;
import com.mtfm.deadman.plugin.logistics.spi.ship.LogisticsShipProvider;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackProvider;
import com.mtfm.deadman.plugin.logistics.spi.waybill.LogisticsWaybillProvider;

import lombok.Getter;

/**
 * 物流各领域 Provider 统一注册中心。
 */
@Component
@Getter
public class LogisticsProviderRegistry {

    private final LogisticsCapabilityRegistry<LogisticsTrackProvider> trackRegistry;
    private final LogisticsCapabilityRegistry<LogisticsCarrierProvider> carrierRegistry;
    private final LogisticsCapabilityRegistry<LogisticsWaybillProvider> waybillRegistry;
    private final LogisticsCapabilityRegistry<LogisticsShipProvider> shipRegistry;
    private final String defaultProviderId;

    /**
     * 构造各领域 Provider 注册中心。
     *
     * @param trackProviders     轨迹 Provider 列表
     * @param carrierProviders   识别 Provider 列表
     * @param waybillProviders   面单 Provider 列表
     * @param shipProviders      寄件 Provider 列表
     * @param logisticsProperties 物流配置
     */
    public LogisticsProviderRegistry(
            List<LogisticsTrackProvider> trackProviders,
            List<LogisticsCarrierProvider> carrierProviders,
            List<LogisticsWaybillProvider> waybillProviders,
            List<LogisticsShipProvider> shipProviders,
            LogisticsPluginProperties logisticsProperties) {
        this.defaultProviderId = logisticsProperties.getDefaultProvider();
        this.trackRegistry = new LogisticsCapabilityRegistry<>(trackProviders, "轨迹");
        this.carrierRegistry = new LogisticsCapabilityRegistry<>(carrierProviders, "识别");
        this.waybillRegistry = new LogisticsCapabilityRegistry<>(waybillProviders, "面单");
        this.shipRegistry = new LogisticsCapabilityRegistry<>(shipProviders, "寄件");
    }

    /**
     * 获取轨迹 Provider。
     *
     * @param providerId 提供商标识，为空时使用默认
     * @return 轨迹 Provider
     */
    public LogisticsTrackProvider requireTrack(String providerId) {
        return trackRegistry.require(providerId, defaultProviderId);
    }

    /**
     * 获取识别 Provider。
     *
     * @param providerId 提供商标识，为空时使用默认
     * @return 识别 Provider
     */
    public LogisticsCarrierProvider requireCarrier(String providerId) {
        return carrierRegistry.require(providerId, defaultProviderId);
    }

    /**
     * 获取面单 Provider。
     *
     * @param providerId 提供商标识，为空时使用默认
     * @return 面单 Provider
     */
    public LogisticsWaybillProvider requireWaybill(String providerId) {
        return waybillRegistry.require(providerId, defaultProviderId);
    }

    /**
     * 获取寄件 Provider。
     *
     * @param providerId 提供商标识，为空时使用默认
     * @return 寄件 Provider
     */
    public LogisticsShipProvider requireShip(String providerId) {
        return shipRegistry.require(providerId, defaultProviderId);
    }
}
