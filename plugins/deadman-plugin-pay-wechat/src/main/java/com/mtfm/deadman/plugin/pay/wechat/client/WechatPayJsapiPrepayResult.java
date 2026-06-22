package com.mtfm.deadman.plugin.pay.wechat.client;

import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayRequestPaymentParams;

/**
 * 微信 JSAPI 预下单结果。
 *
 * @param prepayId        微信 prepay_id
 * @param requestPayment  小程序调起支付参数
 */
public record WechatPayJsapiPrepayResult(String prepayId, WechatPayRequestPaymentParams requestPayment) {
}
