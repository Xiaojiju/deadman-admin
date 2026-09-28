package com.mtfm.deadman.plugin.pay.support;

import com.mtfm.deadman.common.exception.BusinessException;

/**
 * 按微信支付业务码构造异常。文案键在微信支付插件资源包的 result.WECHAT_PAY_*。
 */
public final class WechatPayMessages {

    private WechatPayMessages() {}

    /**
     * 使用模块文案构造业务异常。
     *
     * @param code 业务码
     * @return 业务异常
     */
    public static BusinessException of(int code) {
        return switch (code) {
            case WechatPayErrorCodes.WECHAT_PAY_ORDER_NOT_FOUND ->
                    ex(code, "result.WECHAT_PAY_ORDER_NOT_FOUND", "支付单不存在");
            case WechatPayErrorCodes.WECHAT_PAY_OPENID_REQUIRED ->
                    ex(code, "result.WECHAT_PAY_OPENID_REQUIRED", "缺少付款人 openid");
            case WechatPayErrorCodes.WECHAT_PAY_PREPAY_FAILED ->
                    ex(code, "result.WECHAT_PAY_PREPAY_FAILED", "微信预下单失败");
            case WechatPayErrorCodes.WECHAT_PAY_CONFIG_INVALID ->
                    ex(code, "result.WECHAT_PAY_CONFIG_INVALID", "微信支付配置无效");
            case WechatPayErrorCodes.WECHAT_PAY_REFUND_FAILED ->
                    ex(code, "result.WECHAT_PAY_REFUND_FAILED", "微信退款申请失败");
            case WechatPayErrorCodes.WECHAT_PAY_ABNORMAL_REFUND_FAILED ->
                    ex(code, "result.WECHAT_PAY_ABNORMAL_REFUND_FAILED", "微信异常退款申请失败");
            case WechatPayErrorCodes.WECHAT_PAY_TRANSFER_FAILED ->
                    ex(code, "result.WECHAT_PAY_TRANSFER_FAILED", "微信商家转账失败");
            case WechatPayErrorCodes.WECHAT_PAY_SCORE_FAILED ->
                    ex(code, "result.WECHAT_PAY_SCORE_FAILED", "微信支付分调用失败");
            default -> new BusinessException(code, "未知错误");
        };
    }

    private static BusinessException ex(int code, String key, String fallback) {
        return new BusinessException(code, key, fallback);
    }
}
