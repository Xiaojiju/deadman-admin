package com.mtfm.deadman.plugin.pay.spi.profitsharing;

/**
 * 分账回退请求。
 *
 * @param subMchid       二级商户号
 * @param outOrderNo     原分账单号
 * @param channelOrderId 微信分账单号（可空，与 outOrderNo 二选一优先）
 * @param outReturnNo    平台回退单号
 * @param returnMchid    回退接收商户号（通常为服务商）
 * @param amountCents    回退金额（分）
 * @param description    回退描述
 */
public record ProfitSharingReturnRequest(
        String subMchid,
        String outOrderNo,
        String channelOrderId,
        String outReturnNo,
        String returnMchid,
        int amountCents,
        String description) {
}
