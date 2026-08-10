package com.mtfm.deadman.plugin.pay.spi.transfer;

/**
 * 渠道转账申请结果。
 *
 * @param outBillNo     平台转账单号
 * @param channelBillNo 渠道转账单号
 * @param targetStatus  目标状态
 * @param packageInfo   用户确认收款 package（可选）
 * @param failReason    失败原因（可选）
 * @param rawPayload    渠道原文（可选）
 */
public record TransferResult(
        String outBillNo,
        String channelBillNo,
        String targetStatus,
        String packageInfo,
        String failReason,
        String rawPayload) {
}
