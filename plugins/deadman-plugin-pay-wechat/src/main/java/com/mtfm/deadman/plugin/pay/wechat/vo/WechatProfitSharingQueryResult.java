package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 收付通分账查询结果。
 *
 * @param subMchid      子商户号
 * @param transactionId 微信订单号
 * @param outOrderNo    商户分账单号
 * @param orderId       微信分账单号
 * @param status        分账单状态
 * @param receiversRaw  接收方原始 JSON（渠道返回原文，便于排查）
 */
public record WechatProfitSharingQueryResult(
        String subMchid,
        String transactionId,
        String outOrderNo,
        String orderId,
        String status,
        String receiversRaw) {
}
