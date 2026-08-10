package com.mtfm.deadman.plugin.crypto.constant;

/**
 * 业务模块密钥命名空间常量（可选便利项）。
 *
 * <p>与配置 {@code deadman.plugin.crypto.module-keys.&lt;code&gt;} 的键对应。
 * 业务模块也可在各自包内自建常量（推荐），不必依赖本类。
 */
public final class CryptoModuleCodes {

    /**
     * 支付扩展模块编码约定。
     * 配置键：{@code deadman.plugin.crypto.module-keys.pay}
     */
    public static final String PAY = "pay";

    private CryptoModuleCodes() {
    }
}
