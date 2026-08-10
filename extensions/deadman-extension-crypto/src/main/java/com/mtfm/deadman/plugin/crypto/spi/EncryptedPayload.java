package com.mtfm.deadman.plugin.crypto.spi;

/**
 * 策略与编解码器之间传递的结构化密文载荷（业务侧无需直接使用）。
 *
 * <p>经 {@link com.mtfm.deadman.plugin.crypto.util.CryptoPayloadCodec} 编码后成为可落库字符串。
 *
 * @param version    载荷格式版本，当前固定为 {@code 1}
 * @param algorithm  算法标识（决定解密时选用哪套策略）
 * @param keyId      包装 DEK 所用 KEK 的标识（解密时按此取密钥）
 * @param wrappedDek 被 KEK 包装后的 DEK（具体布局由策略定义，如 IV||密文）
 * @param iv         数据加密用 IV / nonce
 * @param ciphertext 数据密文（含认证标签，如 GCM tag）
 */
public record EncryptedPayload(
        int version,
        String algorithm,
        String keyId,
        byte[] wrappedDek,
        byte[] iv,
        byte[] ciphertext) {
}
