package com.mtfm.deadman.plugin.pay.spi.transfer;

/**
 * 渠道转账回调解析结果。
 *
 * @param outBillNo     平台转账单号
 * @param channelBillNo 渠道转账单号
 * @param amountCents   金额（分，可选）
 * @param targetStatus  目标状态
 * @param failReason    失败原因
 * @param rawPayload    原文
 */
public record TransferNotifyResult(
        String outBillNo,
        String channelBillNo,
        Long amountCents,
        String targetStatus,
        String failReason,
        String rawPayload) {
}
