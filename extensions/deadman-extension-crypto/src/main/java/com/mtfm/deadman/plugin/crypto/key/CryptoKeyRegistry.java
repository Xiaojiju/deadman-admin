package com.mtfm.deadman.plugin.crypto.key;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.crypto.config.CryptoPluginProperties;
import com.mtfm.deadman.plugin.crypto.util.CryptoTokenIds;

import lombok.extern.slf4j.Slf4j;

/**
 * 密钥注册表：维护「默认 KEK + 各模块 KEK + 附加历史密钥」多轨道，并按场景解析。
 *
 * <h2>加密解析规则（{@link #resolveForEncrypt}）</h2>
 * <ol>
 *   <li>若传入 {@code moduleCode} 且配置了对应 {@code module-keys}，使用该模块密钥。</li>
 *   <li>若指定了模块但未配置模块密钥：{@code strict-module-key=true} 时失败，否则回退 default-key。</li>
 *   <li>未指定模块时使用 {@code default-key}。</li>
 * </ol>
 *
 * <h2>解密解析规则（{@link #requireByKeyId}）</h2>
 * <p>严格按密文中的 {@code keyId} 取密钥（含 {@code additional-keys} 中的历史密钥）。
 *
 * <p>密钥材料要求：Base64 解码后恰好 32 字节（AES-256）；keyId 不得含点号。
 */
@Slf4j
public class CryptoKeyRegistry {

    /** AES-256 原始密钥字节长度 */
    private static final int AES_256_KEY_BYTES = 32;

    /** 配置引用（读取 defaultKeyId、strictModuleKey 等） */
    private final CryptoPluginProperties properties;
    /** keyId → KEK */
    private final Map<String, SecretKey> keysById;
    /** moduleCode → keyId */
    private final Map<String, String> moduleToKeyId;

    /**
     * 启动时从配置加载全部可用密钥。
     *
     * @param properties 加解密配置
     */
    public CryptoKeyRegistry(CryptoPluginProperties properties) {
        this.properties = properties;
        Map<String, SecretKey> byId = new LinkedHashMap<>();
        Map<String, String> moduleMap = new LinkedHashMap<>();

        if (StringUtils.hasText(properties.getDefaultKey())) {
            String defaultKeyId = CryptoTokenIds.requireSafe(
                    StringUtils.hasText(properties.getDefaultKeyId())
                            ? properties.getDefaultKeyId()
                            : "default",
                    "defaultKeyId");
            byId.put(defaultKeyId, parseAes256Key(properties.getDefaultKey(), "default"));
            log.info("已加载加解密默认密钥：keyId={}", defaultKeyId);
        } else {
            log.warn("未配置 deadman.plugin.crypto.default-key，仅模块/附加密钥可用");
        }

        if (properties.getModuleKeys() != null) {
            for (Map.Entry<String, CryptoPluginProperties.ModuleKeyProperties> entry :
                    properties.getModuleKeys().entrySet()) {
                String moduleCode = entry.getKey() == null ? null : entry.getKey().trim();
                CryptoPluginProperties.ModuleKeyProperties moduleKey = entry.getValue();
                if (!StringUtils.hasText(moduleCode) || moduleKey == null || !StringUtils.hasText(moduleKey.getKey())) {
                    continue;
                }
                CryptoTokenIds.requireSafe(moduleCode, "moduleCode");
                String keyId = CryptoTokenIds.requireSafe(
                        StringUtils.hasText(moduleKey.getKeyId()) ? moduleKey.getKeyId() : moduleCode,
                        "moduleKeyId");
                byId.put(keyId, parseAes256Key(moduleKey.getKey(), "module:" + moduleCode));
                moduleMap.put(moduleCode, keyId);
                log.info("已加载模块加解密密钥：module={}, keyId={}", moduleCode, keyId);
            }
        }

        if (properties.getAdditionalKeys() != null) {
            for (Map.Entry<String, String> entry : properties.getAdditionalKeys().entrySet()) {
                if (!StringUtils.hasText(entry.getKey()) || !StringUtils.hasText(entry.getValue())) {
                    continue;
                }
                String keyId = CryptoTokenIds.requireSafe(entry.getKey(), "additionalKeyId");
                if (byId.containsKey(keyId)) {
                    log.warn("附加密钥 keyId={} 与已有密钥冲突，跳过附加项", keyId);
                    continue;
                }
                byId.put(keyId, parseAes256Key(entry.getValue(), "additional:" + keyId));
                log.info("已加载附加（轮换）解密密钥：keyId={}", keyId);
            }
        }

        this.keysById = Map.copyOf(byId);
        this.moduleToKeyId = Map.copyOf(moduleMap);
    }

    /**
     * 按模块解析加密用 KEK：优先模块密钥；严格模式下禁止静默回退。
     *
     * @param moduleCode 模块编码，可空（空则直接走默认轨道）
     * @return keyId 与密钥材料
     */
    public ResolvedKey resolveForEncrypt(String moduleCode) {
        if (StringUtils.hasText(moduleCode)) {
            String normalized = moduleCode.trim();
            String keyId = moduleToKeyId.get(normalized);
            if (keyId != null) {
                return new ResolvedKey(keyId, requireByKeyId(keyId));
            }
            if (properties.isStrictModuleKey()) {
                throw new BusinessException(
                        ResultCode.CRYPTO_KEY_NOT_FOUND,
                        "未配置模块密钥且已开启 strict-module-key：module=" + normalized);
            }
            log.warn("模块密钥未配置，回退默认密钥：module={}", normalized);
        }
        String defaultKeyId = CryptoTokenIds.requireSafe(
                StringUtils.hasText(properties.getDefaultKeyId())
                        ? properties.getDefaultKeyId()
                        : "default",
                "defaultKeyId");
        SecretKey key = keysById.get(defaultKeyId);
        if (key == null) {
            throw new BusinessException(
                    ResultCode.CRYPTO_KEY_NOT_FOUND,
                    "未配置可用加密密钥（模块=" + moduleCode + "，且无 default-key）");
        }
        return new ResolvedKey(defaultKeyId, key);
    }

    /**
     * 按密文中的 keyId 解析解密用 KEK。
     *
     * @param keyId 密钥标识
     * @return KEK
     */
    public SecretKey requireByKeyId(String keyId) {
        if (!StringUtils.hasText(keyId)) {
            throw new BusinessException(ResultCode.CRYPTO_KEY_NOT_FOUND, "密文缺少 keyId");
        }
        SecretKey key = keysById.get(keyId.trim());
        if (key == null) {
            throw new BusinessException(ResultCode.CRYPTO_KEY_NOT_FOUND, "加解密密钥不存在：" + keyId);
        }
        return key;
    }

    /**
     * 是否至少配置了一把可用密钥。
     *
     * @return 有密钥时 true
     */
    public boolean hasAnyKey() {
        return !keysById.isEmpty();
    }

    /**
     * 将 Base64 密钥材料解析为 AES-256 {@link SecretKey}。
     *
     * @param base64Key Base64 编码的 32 字节密钥
     * @param label     日志/异常用标签（如 default、module:pay）
     * @return AES 密钥
     */
    private static SecretKey parseAes256Key(String base64Key, String label) {
        try {
            byte[] raw = Base64.getDecoder().decode(base64Key.trim());
            if (raw.length != AES_256_KEY_BYTES) {
                throw new BusinessException(
                        ResultCode.CRYPTO_CONFIG_INVALID,
                        "密钥长度必须为 32 字节（Base64），当前标签=" + label + "，实际=" + raw.length);
            }
            return new SecretKeySpec(raw, "AES");
        } catch (BusinessException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ResultCode.CRYPTO_CONFIG_INVALID, "密钥 Base64 非法：" + label, ex);
        }
    }

    /**
     * 加密侧解析得到的密钥对：写入密文的标识 + 实际 KEK。
     *
     * @param keyId 密钥标识（写入密文 keyId 段）
     * @param key   密钥加密密钥（KEK）
     */
    public record ResolvedKey(String keyId, SecretKey key) {
    }
}
