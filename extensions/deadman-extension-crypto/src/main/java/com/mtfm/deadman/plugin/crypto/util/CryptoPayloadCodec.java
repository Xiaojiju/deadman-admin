package com.mtfm.deadman.plugin.crypto.util;

import java.util.Base64;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.crypto.spi.EncryptedPayload;

/**
 * 密文载荷与可落库字符串之间的编解码工具。
 *
 * <p>落库格式（点分六段，二进制段为 Base64URL 无填充）：
 * <pre>
 * v1.{algorithm}.{keyId}.{wrappedDek}.{iv}.{ciphertext}
 * </pre>
 *
 * <p>algorithm / keyId 仅允许 {@link CryptoTokenIds} 字符集，避免点号破坏分段。
 */
public final class CryptoPayloadCodec {

    /** 本模块密文前缀 / 版本标记 */
    public static final String PREFIX = "v1.";

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private CryptoPayloadCodec() {
    }

    /**
     * 判断字符串是否为本模块可识别的密文 token。
     *
     * @param value 待判断字符串
     * @return 以 {@code v1.} 开头时为 true
     */
    public static boolean isEncryptedToken(String value) {
        return StringUtils.hasText(value) && value.startsWith(PREFIX);
    }

    /**
     * 将结构化载荷编码为可落库密文字符串。
     *
     * @param payload 结构化密文
     * @return 点分密文 token
     */
    public static String encode(EncryptedPayload payload) {
        String algorithm = CryptoTokenIds.requireSafe(payload.algorithm(), "algorithm");
        String keyId = CryptoTokenIds.requireSafe(payload.keyId(), "keyId");
        return "v1."
                + algorithm
                + "."
                + keyId
                + "."
                + ENCODER.encodeToString(payload.wrappedDek())
                + "."
                + ENCODER.encodeToString(payload.iv())
                + "."
                + ENCODER.encodeToString(payload.ciphertext());
    }

    /**
     * 解析密文 token 为结构化载荷。
     *
     * @param token 密文字符串
     * @return 结构化密文
     * @throws BusinessException 格式非法或 Base64 非法时
     */
    public static EncryptedPayload decode(String token) {
        if (!isEncryptedToken(token)) {
            throw new BusinessException(ResultCode.CRYPTO_PAYLOAD_INVALID, "密文格式无效");
        }
        String[] parts = token.split("\\.", 6);
        if (parts.length != 6 || !"v1".equals(parts[0])) {
            throw new BusinessException(ResultCode.CRYPTO_PAYLOAD_INVALID, "密文分段无效");
        }
        try {
            String algorithm = CryptoTokenIds.requireSafe(parts[1], "algorithm");
            String keyId = CryptoTokenIds.requireSafe(parts[2], "keyId");
            return new EncryptedPayload(
                    1,
                    algorithm,
                    keyId,
                    DECODER.decode(parts[3]),
                    DECODER.decode(parts[4]),
                    DECODER.decode(parts[5]));
        } catch (BusinessException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ResultCode.CRYPTO_PAYLOAD_INVALID, "密文 Base64 非法", ex);
        }
    }
}
