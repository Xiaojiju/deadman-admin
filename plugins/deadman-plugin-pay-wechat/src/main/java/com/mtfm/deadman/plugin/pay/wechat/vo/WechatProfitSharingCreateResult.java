package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 收付通请求分账结果。
 *
 * @param subMchid      子商户号
 * @param transactionId 微信订单号
 * @param outOrderNo    商户分账单号
 * @param orderId       微信分账单号
 * @param status        分账单状态
 */
public record WechatProfitSharingCreateResult(
        String subMchid, String transactionId, String outOrderNo, String orderId, String status) {
}
