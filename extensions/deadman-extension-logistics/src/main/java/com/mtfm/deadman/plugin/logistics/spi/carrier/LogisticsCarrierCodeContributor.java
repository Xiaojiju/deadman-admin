package com.mtfm.deadman.plugin.logistics.spi.carrier;

import java.util.Map;

/**
 * 快递公司编码映射贡献者：各渠道插件注册「平台统一编码 → 厂商编码」对照表。
 * <p>
 * 实现类注册为 Spring Bean 后，由 {@link com.mtfm.deadman.plugin.logistics.manager.LogisticsCarrierCodeRegistry}
 * 在应用启动完成后自动聚合。
 */
public interface LogisticsCarrierCodeContributor {

    /**
     * 本贡献者对应的物流 Provider 标识。
     *
     * @return Provider 标识，如 {@code kuaidi100}
     */
    String providerId();

    /**
     * 贡献统一编码到厂商编码的映射。
     *
     * @return key 为 {@link LogisticsCarriers} 等平台统一编码，value 为厂商 API 编码
     */
    Map<String, String> contribute();
}
