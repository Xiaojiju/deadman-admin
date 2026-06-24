package com.mtfm.deadman.plugin.logistics.spi;

/**
 * 物流能力 Provider 基础契约，各领域 Provider 需声明唯一标识。
 */
public interface LogisticsCapabilityProvider {

    /**
     * Provider 唯一标识，同一渠道在各领域下应保持一致（如 {@code kuaidi100}）。
     *
     * @return 提供商标识
     */
    String providerId();
}
