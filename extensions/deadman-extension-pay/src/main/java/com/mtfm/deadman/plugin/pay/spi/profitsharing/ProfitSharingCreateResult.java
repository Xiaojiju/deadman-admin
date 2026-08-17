package com.mtfm.deadman.plugin.pay.spi.profitsharing;

/**
 * 请求分账结果。
 *
 * @param subMchid       二级商户号
 * @param transactionId  微信交易号
 * @param outOrderNo     平台分账单号
 * @param channelOrderId 微信分账单号
 * @param status         渠道状态
 */
public record ProfitSharingCreateResult(
        String subMchid,
        String transactionId,
        String outOrderNo,
        String channelOrderId,
        String status) {
}
