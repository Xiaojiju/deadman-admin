package com.mtfm.deadman.plugin.pay.vo.transfer;

import java.time.LocalDateTime;

/**
 * 转账明细视图。
 *
 * @param outBillNo     平台转账单号
 * @param batchNo       批次号
 * @param seqNo         序号
 * @param amountCents   金额（分）
 * @param status        状态
 * @param channelBillNo 渠道单号
 * @param packageInfo   用户确认 package
 * @param failReason    失败原因
 * @param dispatchedTime 派发时间
 * @param finishedTime  终态时间
 * @param createTime    创建时间
 */
public record TransferBillVO(
        String outBillNo,
        String batchNo,
        Integer seqNo,
        Long amountCents,
        String status,
        String channelBillNo,
        String packageInfo,
        String failReason,
        LocalDateTime dispatchedTime,
        LocalDateTime finishedTime,
        LocalDateTime createTime) {
}
