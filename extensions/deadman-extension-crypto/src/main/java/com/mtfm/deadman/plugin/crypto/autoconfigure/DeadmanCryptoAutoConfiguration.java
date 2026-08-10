package com.mtfm.deadman.plugin.crypto.autoconfigure;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.mtfm.deadman.plugin.crypto.config.CryptoPluginProperties;
import com.mtfm.deadman.plugin.crypto.facade.CryptoFacade;
import com.mtfm.deadman.plugin.crypto.facade.DefaultCryptoFacade;
import com.mtfm.deadman.plugin.crypto.key.CryptoKeyRegistry;
import com.mtfm.deadman.plugin.crypto.spi.EncryptionStrategy;
import com.mtfm.deadman.plugin.crypto.strategy.AesGcmEnvelopeEncryptionStrategy;

/**
 * 加解密插件自动配置。
 *
 * <p>当 {@code deadman.plugin.crypto.enabled=true}（默认）时注册：
 * <ul>
 *   <li>{@link AesGcmEnvelopeEncryptionStrategy} — 默认信封策略（显式 Bean，策略类无 Spring 刻板注解）</li>
 *   <li>{@link CryptoKeyRegistry} — 密钥双轨道 + 附加轮换密钥；{@code require-keys} 时启动校验</li>
 *   <li>{@link CryptoFacade} — 对外统一门面</li>
 * </ul>
 *
 * <p>无 Spring 场景请使用 {@link com.mtfm.deadman.plugin.crypto.facade.CryptoFacades#create}。
 */
@AutoConfiguration
@EnableConfigurationProperties(CryptoPluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.crypto", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DeadmanCryptoAutoConfiguration {

    /**
     * 默认 AES-GCM 信封策略。
     *
     * @return 策略实现
     */
    @Bean
    @ConditionalOnMissingBean(AesGcmEnvelopeEncryptionStrategy.class)
    public AesGcmEnvelopeEncryptionStrategy aesGcmEnvelopeEncryptionStrategy() {
        return new AesGcmEnvelopeEncryptionStrategy();
    }

    /**
     * 注册密钥表：加载 default-key、module-keys、additional-keys。
     *
     * @param properties 加解密配置
     * @return 密钥注册表
     */
    @Bean
    @ConditionalOnMissingBean
    public CryptoKeyRegistry cryptoKeyRegistry(CryptoPluginProperties properties) {
        CryptoKeyRegistry registry = new CryptoKeyRegistry(properties);
        if (properties.isRequireKeys() && !registry.hasAnyKey()) {
            throw new IllegalStateException(
                    "deadman.plugin.crypto.require-keys=true，但未配置任何密钥（default-key / module-keys / additional-keys）。"
                            + "请用环境变量注入 Base64(32 字节) 密钥，例如：openssl rand -base64 32");
        }
        return registry;
    }

    /**
     * 注册加解密门面，供业务统一调用。
     *
     * @param properties  加解密配置
     * @param keyRegistry 密钥注册表
     * @param strategies  全部加密策略实现
     * @return 加解密门面
     */
    @Bean
    @ConditionalOnMissingBean(CryptoFacade.class)
    public CryptoFacade cryptoFacade(
            CryptoPluginProperties properties,
            CryptoKeyRegistry keyRegistry,
            List<EncryptionStrategy> strategies) {
        return new DefaultCryptoFacade(properties, keyRegistry, strategies);
    }
}
