package com.mtfm.deadman.plugin.pay.support;

/**
 * 微信支付渠道业务码。放在支付扩展里，渠道插件和支付门面都能引用。
 * 数值与历史接口保持一致，文案键仍为 result.WECHAT_PAY_*，文案在微信支付插件资源包。
 */
public final class WechatPayErrorCodes {

    /** 支付单不存在 */
    public static final int WECHAT_PAY_ORDER_NOT_FOUND = 14001;

    /** 缺少付款人 openid */
    public static final int WECHAT_PAY_OPENID_REQUIRED = 14002;

    /** 微信预下单失败 */
    public static final int WECHAT_PAY_PREPAY_FAILED = 14003;

    /** 微信支付配置无效 */
    public static final int WECHAT_PAY_CONFIG_INVALID = 14004;

    /** 微信退款申请失败 */
    public static final int WECHAT_PAY_REFUND_FAILED = 14005;

    /** 微信异常退款申请失败 */
    public static final int WECHAT_PAY_ABNORMAL_REFUND_FAILED = 14006;

    /** 微信商家转账失败 */
    public static final int WECHAT_PAY_TRANSFER_FAILED = 14007;

    /** 微信支付分调用失败 */
    public static final int WECHAT_PAY_SCORE_FAILED = 14008;

    private WechatPayErrorCodes() {}
}
