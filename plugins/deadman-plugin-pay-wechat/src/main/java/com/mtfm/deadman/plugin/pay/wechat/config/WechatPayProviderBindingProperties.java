package com.mtfm.deadman.plugin.pay.wechat.config;

import lombok.Data;

/**
 * 单个微信支付 Provider 绑定配置（如 wechat-jsapi、wechat-native）。
 */
@Data
public class WechatPayProviderBindingProperties {

    /** 是否启用该 Provider */
    private boolean enabled = false;

    /** 该 Provider 对应的微信 AppId */
    private String appId;

    /** 提交给微信 API 的支付结果回调完整 URL */
    private String notifyUrl;

    /** 本应用接收该 Provider 支付回调的 endpoint 路径 */
    private String notifyEndpoint = "/client/api/pay/wechat/jsapi/notify";

    /** 提交给微信 API 的退款结果回调完整 URL */
    private String refundNotifyUrl;

    /** 本应用接收该 Provider 退款回调的 endpoint 路径 */
    private String refundNotifyEndpoint = "/client/api/pay/wechat/jsapi/refund/notify";

    /** 提交给微信 API 的商家转账结果回调完整 URL */
    private String transferNotifyUrl;

    /** 本应用接收该 Provider 商家转账回调的 endpoint 路径 */
    private String transferNotifyEndpoint = "/client/api/pay/wechat/jsapi/transfer/notify";

    /** 支付分服务 ID */
    private String serviceId;

    /** 提交给微信 API 的支付分结果回调完整 URL */
    private String payScoreNotifyUrl;

    /** 本应用接收该 Provider 支付分回调的 endpoint 路径 */
    private String payScoreNotifyEndpoint = "/client/api/pay/wechat/payscore/notify";
}
