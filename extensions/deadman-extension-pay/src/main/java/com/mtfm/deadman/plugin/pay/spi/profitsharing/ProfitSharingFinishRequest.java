package com.mtfm.deadman.plugin.pay.spi.profitsharing;

/**
 * 完结分账请求（解冻剩余货款给二级商户）。
 *
 * @param subMchid      二级商户号
 * @param transactionId 微信交易号
 * @param outOrderNo    平台完结单号（可与分账单号区分）
 * @param description   完结描述
 */
public record ProfitSharingFinishRequest(
        String subMchid, String transactionId, String outOrderNo, String description) {
}
