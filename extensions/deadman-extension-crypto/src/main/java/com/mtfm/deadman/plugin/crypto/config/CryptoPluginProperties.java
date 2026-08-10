package com.mtfm.deadman.plugin.crypto.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import com.mtfm.deadman.plugin.crypto.constant.CryptoAlgorithms;

import lombok.Data;

/**
 * 加解密插件配置：默认密钥 + 模块密钥双轨道，并支持轮换附加密钥。
 *
 * <p>配置前缀：{@code deadman.plugin.crypto}。
 *
 * <pre>{@code
 * deadman.plugin.crypto.enabled=true
 * deadman.plugin.crypto.require-keys=true
 * deadman.plugin.crypto.strict-module-key=true
 * deadman.plugin.crypto.default-key=${DEADMAN_CRYPTO_DEFAULT_KEY}
 * deadman.plugin.crypto.module-keys.pay.key=${DEADMAN_CRYPTO_PAY_KEY}
 * deadman.plugin.crypto.additional-keys.pay-v1=${DEADMAN_CRYPTO_PAY_V1_KEY}
 * }</pre>
 *
 * <p>密钥均为 Base64 编码的 32 字节；生产环境务必用环境变量注入，禁止写入仓库。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.crypto")
public class CryptoPluginProperties {

    /** 是否启用加解密能力；关闭后加解密接口 fail-closed 抛错（不再静默透传明文） */
    private boolean enabled = true;

    /**
     * 启用时若未配置任何密钥则启动失败。
     * 防止「以为开了加密、实际无密钥」导致运行期才暴露问题。
     */
    private boolean requireKeys = true;

    /**
     * 调用方指定了 moduleCode 但未配置对应模块密钥时，是否禁止回退 default-key。
     * 为 true 时直接失败，避免误以为已做模块隔离。
     */
    private boolean strictModuleKey = true;

    /** 默认算法标识，需与已注册策略的 {@code algorithmId} 一致 */
    private String defaultAlgorithm = CryptoAlgorithms.AES_GCM_ENVELOPE;

    /** 默认密钥标识（写入密文 keyId 段）；未配置模块密钥时使用 */
    private String defaultKeyId = "default";

    /**
     * 默认主密钥（KEK）：Base64 编码的 32 字节 AES-256 密钥。
     * 生产必须通过环境变量注入，禁止提交真实密钥。
     */
    private String defaultKey;

    /**
     * 各业务模块独立 KEK。
     * Map 键为模块编码（如 {@code pay}），与 {@code CryptoModuleCodes} / {@code CryptoContext.moduleCode} 对应。
     */
    @NestedConfigurationProperty
    private Map<String, ModuleKeyProperties> moduleKeys = new LinkedHashMap<>();

    /**
     * 仅用于解密的附加/历史密钥（密钥轮换）：keyId → Base64(32 字节)。
     * 加密不会选用这些 keyId，除非某模块当前 keyId 恰好与之相同。
     */
    private Map<String, String> additionalKeys = new LinkedHashMap<>();

    /**
     * 单个模块的密钥配置项。
     */
    @Data
    public static class ModuleKeyProperties {

        /**
         * 写入密文的密钥标识；为空时使用模块编码本身。
         * 仅允许字母数字下划线与连字符。
         */
        private String keyId;

        /**
         * 模块 KEK：Base64 编码的 32 字节 AES-256 密钥。
         */
        private String key;
    }
}
