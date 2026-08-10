package com.mtfm.deadman.plugin.crypto.facade;

import java.util.List;

import com.mtfm.deadman.plugin.crypto.config.CryptoPluginProperties;
import com.mtfm.deadman.plugin.crypto.key.CryptoKeyRegistry;
import com.mtfm.deadman.plugin.crypto.spi.EncryptionStrategy;
import com.mtfm.deadman.plugin.crypto.strategy.AesGcmEnvelopeEncryptionStrategy;

/**
 * 加解密门面工厂：支持在<strong>无 Spring 容器</strong>场景下手动组装。
 *
 * <p>
 * 示例：
 * 
 * <pre>{@code
 * CryptoPluginProperties props = new CryptoPluginProperties();
 * props.setEnabled(true);
 * props.setDefaultKey(Base64.getEncoder().encodeToString(key32Bytes));
 * CryptoFacade facade = CryptoFacades.create(props);
 * String cipher = facade.encrypt("张三");
 * }</pre>
 *
 * <p>
 * Spring Boot 环境请直接注入 {@link CryptoFacade}，无需调用本工厂。
 */
public final class CryptoFacades {

    private CryptoFacades() {
    }

    /**
     * 使用默认 AES-GCM 信封策略创建门面。
     *
     * @param properties 加解密配置（须已设置密钥等）
     * @return 门面实例
     */
    public static CryptoFacade create(CryptoPluginProperties properties) {
        return create(properties, List.of(new AesGcmEnvelopeEncryptionStrategy()));
    }

    /**
     * 使用自定义策略列表创建门面。
     *
     * @param properties 加解密配置
     * @param strategies 策略实现（至少一种）
     * @return 门面实例
     */
    public static CryptoFacade create(CryptoPluginProperties properties, List<EncryptionStrategy> strategies) {
        if (properties == null) {
            throw new IllegalArgumentException("properties 不能为空");
        }
        if (strategies == null || strategies.isEmpty()) {
            throw new IllegalArgumentException("strategies 不能为空");
        }
        CryptoKeyRegistry registry = new CryptoKeyRegistry(properties);
        if (properties.isEnabled() && properties.isRequireKeys() && !registry.hasAnyKey()) {
            throw new IllegalStateException(
                    "加解密已启用且 require-keys=true，但未配置任何密钥（default-key / module-keys / additional-keys）");
        }
        return new DefaultCryptoFacade(properties, registry, List.copyOf(strategies));
    }
}
