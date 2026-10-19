package com.mtfm.deadman.plugin.crypto.support;

import com.mtfm.deadman.common.exception.BusinessException;

/**
 * 按业务码构造异常，文案走本模块资源包中的 result.* 键。
 */
public final class CryptoMessages {

    private CryptoMessages() {}

    /**
     * 使用模块文案构造业务异常。
     *
     * @param code 业务码
     * @return 业务异常
     */
    public static BusinessException of(int code) {
        return switch (code) {
            case CryptoErrorCodes.CRYPTO_CONFIG_INVALID -> ex(code, "result.CRYPTO_CONFIG_INVALID", "加解密配置无效");
            case CryptoErrorCodes.CRYPTO_KEY_NOT_FOUND -> ex(code, "result.CRYPTO_KEY_NOT_FOUND", "加解密密钥不存在");
            case CryptoErrorCodes.CRYPTO_ALGORITHM_UNSUPPORTED -> ex(code, "result.CRYPTO_ALGORITHM_UNSUPPORTED", "不支持的加解密算法");
            case CryptoErrorCodes.CRYPTO_ENCRYPT_FAILED -> ex(code, "result.CRYPTO_ENCRYPT_FAILED", "加密失败");
            case CryptoErrorCodes.CRYPTO_DECRYPT_FAILED -> ex(code, "result.CRYPTO_DECRYPT_FAILED", "解密失败");
            case CryptoErrorCodes.CRYPTO_PAYLOAD_INVALID -> ex(code, "result.CRYPTO_PAYLOAD_INVALID", "密文格式无效");
            default -> new BusinessException(code, "未知错误");
        };
    }

    private static BusinessException ex(int code, String key, String fallback) {
        return new BusinessException(code, key, fallback);
    }
}
