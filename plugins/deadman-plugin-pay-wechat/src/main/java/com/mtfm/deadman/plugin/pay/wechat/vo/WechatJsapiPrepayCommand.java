package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 微信 JSAPI 预下单命令，携带 Provider 维度的 AppId 与回调地址。
 *
 * @param outTradeNo  平台支付单号
 * @param description 商品描述
 * @param amountTotal 金额（分）
 * @param openid      付款人 openid
 * @param appId       微信 AppId
 * @param notifyUrl   支付结果回调 URL
 */
public record WechatJsapiPrepayCommand(
        String outTradeNo, String description, int amountTotal, String openid, String appId, String notifyUrl) {
}
