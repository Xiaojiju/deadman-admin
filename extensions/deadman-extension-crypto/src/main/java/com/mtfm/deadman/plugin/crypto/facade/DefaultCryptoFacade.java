package com.mtfm.deadman.plugin.crypto.facade;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.crypto.config.CryptoPluginProperties;
import com.mtfm.deadman.plugin.crypto.key.CryptoKeyRegistry;
import com.mtfm.deadman.plugin.crypto.spi.CryptoContext;
import com.mtfm.deadman.plugin.crypto.spi.EncryptedPayload;
import com.mtfm.deadman.plugin.crypto.spi.EncryptionStrategy;
import com.mtfm.deadman.plugin.crypto.util.CryptoAad;
import com.mtfm.deadman.plugin.crypto.util.CryptoPayloadCodec;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link CryptoFacade} 默认实现：按算法策略 + 密钥注册表完成加解密。
 *
 * <p>处理流程概要：
 * <ol>
 *   <li>加密：解析算法 → 按模块解析 KEK → 构造 AAD → 策略信封加密 → 编码落库串。</li>
 *   <li>解密：识别密文 → 解码 → 按 keyId 取 KEK → 用调用方上下文 AAD 解密。</li>
 * </ol>
 *
 * <p>业务侧请只注入 {@link CryptoFacade}，或通过 {@link CryptoFacades} 工厂创建；不要直接依赖本类。
 *
 * @see CryptoFacade
 * @see CryptoFacades
 */
@Slf4j
public class DefaultCryptoFacade implements CryptoFacade {

    /** 插件配置（开关、默认算法等） */
    private final CryptoPluginProperties properties;
    /** 默认 / 模块 / 附加密钥 */
    private final CryptoKeyRegistry keyRegistry;
    /** 算法标识 → 策略实现 */
    private final Map<String, EncryptionStrategy> strategies;
    /** 配置的默认算法；上下文未指定时使用 */
    private final String defaultAlgorithm;

    /**
     * 组装策略表并确定默认算法。
     *
     * @param properties   加解密插件配置
     * @param keyRegistry  密钥注册表
     * @param strategyList 全部 {@link EncryptionStrategy} 实现
     */
    public DefaultCryptoFacade(
            CryptoPluginProperties properties,
            CryptoKeyRegistry keyRegistry,
            List<EncryptionStrategy> strategyList) {
        this.properties = properties;
        this.keyRegistry = keyRegistry;
        Map<String, EncryptionStrategy> map = new LinkedHashMap<>();
        for (EncryptionStrategy strategy : strategyList) {
            map.put(strategy.algorithmId(), strategy);
        }
        this.strategies = Map.copyOf(map);
        this.defaultAlgorithm = StringUtils.hasText(properties.getDefaultAlgorithm())
                ? properties.getDefaultAlgorithm().trim()
                : strategies.keySet().stream().findFirst().orElse(null);
        log.info("加解密门面已就绪，策略={}，默认算法={}", strategies.keySet(), defaultAlgorithm);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String encrypt(String plaintext, CryptoContext context) {
        if (!StringUtils.hasText(plaintext)) {
            return plaintext;
        }
        return encryptBytes(
                plaintext.getBytes(StandardCharsets.UTF_8),
                context == null ? CryptoContext.defaults() : context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String encryptBytes(byte[] plaintext, CryptoContext context) {
        if (plaintext == null || plaintext.length == 0) {
            return "";
        }
        ensureEnabled();
        if (!keyRegistry.hasAnyKey()) {
            throw new BusinessException(ResultCode.CRYPTO_KEY_NOT_FOUND, "未配置任何加解密密钥");
        }
        CryptoContext ctx = context == null ? CryptoContext.defaults() : context;
        EncryptionStrategy strategy = requireStrategy(ctx.algorithmId());
        CryptoKeyRegistry.ResolvedKey resolved = keyRegistry.resolveForEncrypt(ctx.moduleCode());
        byte[] aad = CryptoAad.from(ctx);
        EncryptedPayload payload = strategy.encrypt(plaintext, resolved.key(), resolved.keyId(), aad);
        return CryptoPayloadCodec.encode(payload);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String decrypt(String ciphertext) {
        return decrypt(ciphertext, CryptoContext.defaults());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String decrypt(String ciphertext, CryptoContext context) {
        if (!StringUtils.hasText(ciphertext)) {
            return ciphertext;
        }
        if (!CryptoPayloadCodec.isEncryptedToken(ciphertext)) {
            // 兼容历史明文：非本模块 token 直接透传
            return ciphertext;
        }
        byte[] plain = decryptBytes(ciphertext, context == null ? CryptoContext.defaults() : context);
        return new String(plain, StandardCharsets.UTF_8);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] decryptBytes(String ciphertext) {
        return decryptBytes(ciphertext, CryptoContext.defaults());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] decryptBytes(String ciphertext, CryptoContext context) {
        if (!StringUtils.hasText(ciphertext)) {
            return new byte[0];
        }
        ensureEnabled();
        EncryptedPayload payload = CryptoPayloadCodec.decode(ciphertext);
        EncryptionStrategy strategy = requireStrategy(payload.algorithm());
        byte[] aad = CryptoAad.from(context == null ? CryptoContext.defaults() : context);
        return strategy.decrypt(payload, keyRegistry.requireByKeyId(payload.keyId()), aad);
    }

    /**
     * 未启用时统一 fail-closed。
     */
    private void ensureEnabled() {
        if (!properties.isEnabled()) {
            throw new BusinessException(ResultCode.CRYPTO_CONFIG_INVALID, "加解密未启用");
        }
    }

    /**
     * 按算法标识解析策略；空白则回退默认算法。
     *
     * @param algorithmId 算法标识，可空
     * @return 策略实现
     */
    private EncryptionStrategy requireStrategy(String algorithmId) {
        String resolved = StringUtils.hasText(algorithmId) ? algorithmId.trim() : defaultAlgorithm;
        if (!StringUtils.hasText(resolved)) {
            throw new BusinessException(ResultCode.CRYPTO_ALGORITHM_UNSUPPORTED, "未配置默认加解密算法");
        }
        EncryptionStrategy strategy = strategies.get(resolved);
        if (strategy == null) {
            throw new BusinessException(ResultCode.CRYPTO_ALGORITHM_UNSUPPORTED, "不支持的加解密算法：" + resolved);
        }
        return strategy;
    }
}
