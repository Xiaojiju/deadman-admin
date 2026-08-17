package com.mtfm.deadman.plugin.pay.spi.profitsharing;

import java.util.List;

/**
 * 请求分账。
 *
 * @param subMchid      二级商户号
 * @param transactionId 微信交易号
 * @param outOrderNo    平台分账单号
 * @param receivers     接收方列表（仅 MERCHANT_ID）
 * @param finish        是否同时完结（通常 false，完结单独调用）
 */
public record ProfitSharingCreateRequest(
        String subMchid,
        String transactionId,
        String outOrderNo,
        List<ProfitSharingReceiver> receivers,
        boolean finish) {
}
