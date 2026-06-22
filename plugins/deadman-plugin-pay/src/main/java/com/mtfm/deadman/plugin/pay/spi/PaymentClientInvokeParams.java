package com.mtfm.deadman.plugin.pay.spi;

/**
 * 客户端调起支付参数（微信 JSAPI 直接映射 {@code wx.requestPayment} 字段）。
 *
 * @param timeStamp    时间戳（秒）
 * @param nonceStr     随机字符串
 * @param packageValue prepay_id 包装值，形如 prepay_id=wx...
 * @param signType     签名类型
 * @param paySign      签名
 */
public record PaymentClientInvokeParams(
        String timeStamp, String nonceStr, String packageValue, String signType, String paySign) {
}
