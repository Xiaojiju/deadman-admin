package com.mtfm.deadman.plugin.pay.wechat.constant;

/**
 * 微信支付 APIv3 回调请求头键名。
 */
public final class WechatPayNotifyHeaders {

    /** 微信平台证书序列号 */
    public static final String SERIAL = "Wechatpay-Serial";

    /** 随机串 */
    public static final String NONCE = "Wechatpay-Nonce";

    /** 签名值 */
    public static final String SIGNATURE = "Wechatpay-Signature";

    /** 时间戳 */
    public static final String TIMESTAMP = "Wechatpay-Timestamp";

    private WechatPayNotifyHeaders() {
    }
}
