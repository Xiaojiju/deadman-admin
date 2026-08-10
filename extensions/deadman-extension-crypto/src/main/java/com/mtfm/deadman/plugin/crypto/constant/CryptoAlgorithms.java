package com.mtfm.deadman.plugin.crypto.constant;

/**
 * 加解密算法标识常量。
 *
 * <p>取值须与 {@link com.mtfm.deadman.plugin.crypto.spi.EncryptionStrategy#algorithmId()}、
 * 配置项 {@code deadman.plugin.crypto.default-algorithm} 以及密文中的 algorithm 段保持一致。
 */
public final class CryptoAlgorithms {

    /**
     * AES-256-GCM 对称信封：随机 DEK 加密数据，再用 KEK 包装 DEK。
     *
     * @see com.mtfm.deadman.plugin.crypto.strategy.AesGcmEnvelopeEncryptionStrategy
     */
    public static final String AES_GCM_ENVELOPE = "AES_GCM_ENVELOPE";

    private CryptoAlgorithms() {
    }
}
