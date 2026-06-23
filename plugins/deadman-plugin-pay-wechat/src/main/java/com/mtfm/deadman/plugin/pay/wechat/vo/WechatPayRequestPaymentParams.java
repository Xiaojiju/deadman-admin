package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 小程序 wx.requestPayment 所需参数。
 *
 * @param timeStamp 时间戳（秒）
 * @param nonceStr  随机字符串
 * @param packageValue prepay_id 包装值，形如 prepay_id=wx...
 * @param signType  签名类型，固定 RSA
 * @param paySign   签名
 */
public record WechatPayRequestPaymentParams(
        String timeStamp, String nonceStr, String packageValue, String signType, String paySign) {
}
