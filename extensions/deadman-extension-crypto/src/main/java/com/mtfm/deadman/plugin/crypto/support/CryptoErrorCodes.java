package com.mtfm.deadman.plugin.crypto.support;

/**
 * 加解密模块业务码。数值与历史接口保持一致，文案键仍为 result.CRYPTO_*。
 */
public final class CryptoErrorCodes {

    /** 加解密配置无效 */
    public static final int CRYPTO_CONFIG_INVALID = 14401;

    /** 加解密密钥不存在 */
    public static final int CRYPTO_KEY_NOT_FOUND = 14402;

    /** 不支持的加解密算法 */
    public static final int CRYPTO_ALGORITHM_UNSUPPORTED = 14403;

    /** 加密失败 */
    public static final int CRYPTO_ENCRYPT_FAILED = 14404;

    /** 解密失败 */
    public static final int CRYPTO_DECRYPT_FAILED = 14405;

    /** 密文格式无效 */
    public static final int CRYPTO_PAYLOAD_INVALID = 14406;

    private CryptoErrorCodes() {}
}
