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

    /** 本应用接收该 Provider 回调的 endpoint 路径 */
    private String notifyEndpoint = "/client/api/pay/wechat/jsapi/notify";
}
