package com.mtfm.deadman.plugin.crypto.spi;

import com.mtfm.deadman.plugin.crypto.constant.CryptoAlgorithms;
import com.mtfm.deadman.plugin.crypto.constant.CryptoModuleCodes;

/**
 * 单次加解密请求上下文：决定模块密钥、算法与 AAD 用途绑定。
 *
 * <p>由业务在调用 {@link com.mtfm.deadman.plugin.crypto.facade.CryptoFacade} 时传入。
 * 解密时须传入与加密相同的 {@code purpose}（及模块编码），否则 GCM 校验失败。
 *
 * @param moduleCode  模块编码（如 {@link CryptoModuleCodes#PAY}）；空则使用默认密钥轨道
 * @param algorithmId 算法标识（如 {@link CryptoAlgorithms#AES_GCM_ENVELOPE}）；空则使用配置默认算法
 * @param purpose     用途标识（如 {@code transfer.user_name}）；非空时写入 GCM AAD，防止密文跨字段移植
 */
public record CryptoContext(String moduleCode, String algorithmId, String purpose) {

    /**
     * 默认上下文：默认密钥 + 默认算法 + 无 AAD 用途（兼容历史密文）。
     *
     * @return 上下文
     */
    public static CryptoContext defaults() {
        return new CryptoContext(null, null, null);
    }

    /**
     * 指定模块密钥，算法与用途使用默认（无 AAD purpose）。
     *
     * @param moduleCode 模块编码
     * @return 上下文
     */
    public static CryptoContext forModule(String moduleCode) {
        return new CryptoContext(moduleCode, null, null);
    }

    /**
     * 指定模块与算法。
     *
     * @param moduleCode  模块编码
     * @param algorithmId 算法标识
     * @return 上下文
     */
    public static CryptoContext of(String moduleCode, String algorithmId) {
        return new CryptoContext(moduleCode, algorithmId, null);
    }

    /**
     * 指定模块、算法与用途（AAD）。
     *
     * @param moduleCode  模块编码
     * @param algorithmId 算法标识
     * @param purpose     用途标识
     * @return 上下文
     */
    public static CryptoContext of(String moduleCode, String algorithmId, String purpose) {
        return new CryptoContext(moduleCode, algorithmId, purpose);
    }

    /**
     * 在当前上下文上设置用途（用于字段级 AAD 绑定）。
     *
     * @param purpose 用途标识
     * @return 新上下文
     */
    public CryptoContext withPurpose(String purpose) {
        return new CryptoContext(moduleCode, algorithmId, purpose);
    }
}
