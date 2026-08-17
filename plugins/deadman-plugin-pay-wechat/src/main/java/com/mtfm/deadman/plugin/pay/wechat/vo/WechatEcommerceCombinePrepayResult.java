package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 收付通合单预下单结果。
 *
 * @param prepayId       微信 prepay_id
 * @param requestPayment 小程序调起支付参数
 */
public record WechatEcommerceCombinePrepayResult(
        String prepayId, WechatPayRequestPaymentParams requestPayment) {
}
