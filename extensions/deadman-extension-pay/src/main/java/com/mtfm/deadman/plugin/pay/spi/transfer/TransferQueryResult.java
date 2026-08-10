package com.mtfm.deadman.plugin.pay.spi.transfer;

/**
 * 渠道转账查单结果。
 *
 * @param outBillNo     平台转账单号
 * @param channelBillNo 渠道转账单号
 * @param amountCents   金额（分，可选）
 * @param targetStatus  目标状态
 * @param packageInfo   用户确认 package（可选）
 * @param failReason    失败原因
 * @param rawPayload    原文
 */
public record TransferQueryResult(
        String outBillNo,
        String channelBillNo,
        Long amountCents,
        String targetStatus,
        String packageInfo,
        String failReason,
        String rawPayload) {
}
