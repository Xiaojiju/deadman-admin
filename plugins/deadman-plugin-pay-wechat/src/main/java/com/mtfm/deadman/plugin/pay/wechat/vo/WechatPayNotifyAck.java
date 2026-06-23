package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 微信支付回调应答体，遵循微信 APIv3 协议（非项目统一 {@code Result} 结构）。
 *
 * @param code    应答码，SUCCESS 或 FAIL
 * @param message 应答描述
 */
public record WechatPayNotifyAck(String code, String message) {

    /** 处理成功应答 */
    public static WechatPayNotifyAck success() {
        return new WechatPayNotifyAck("SUCCESS", "成功");
    }

    /** 处理失败应答 */
    public static WechatPayNotifyAck failure() {
        return new WechatPayNotifyAck("FAIL", "失败");
    }
}
