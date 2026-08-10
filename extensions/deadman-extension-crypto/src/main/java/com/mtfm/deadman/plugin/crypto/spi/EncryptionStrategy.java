package com.mtfm.deadman.plugin.crypto.spi;

import javax.crypto.SecretKey;

/**
 * 加解密策略 SPI：门面按 {@link #algorithmId()} 选择具体实现。
 *
 * <p>扩展新算法时：实现本接口，并由自动配置或 {@code CryptoFacades} 工厂注册；
 * 算法标识需与配置及密文中的 algorithm 段一致。
 *
 * <p>约定：策略负责「用 KEK 做信封加解密」及 AAD 绑定；不负责密钥查找与密文字符串编解码。
 */
public interface EncryptionStrategy {

    /**
     * 算法标识，与配置 / 密文中的 algorithm 一致。
     *
     * @return 算法标识，如 {@code AES_GCM_ENVELOPE}
     */
    String algorithmId();

    /**
     * 使用 KEK 对明文做加密（通常为对称信封：随机 DEK 加密数据，再包装 DEK）。
     *
     * @param plaintext 明文字节
     * @param kek       密钥加密密钥
     * @param keyId     KEK 标识，写入载荷
     * @param aad       GCM 关联数据；空数组表示不绑定用途（兼容历史）
     * @return 结构化密文载荷
     */
    EncryptedPayload encrypt(byte[] plaintext, SecretKey kek, String keyId, byte[] aad);

    /**
     * 使用 KEK 解密结构化载荷。
     *
     * @param payload 结构化密文
     * @param kek     与加密时相同的 KEK
     * @param aad     须与加密时相同的关联数据
     * @return 明文字节
     */
    byte[] decrypt(EncryptedPayload payload, SecretKey kek, byte[] aad);
}
