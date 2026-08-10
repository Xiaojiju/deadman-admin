package com.mtfm.deadman.plugin.pay.vo.transfer;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 转账批次视图。
 *
 * @param batchNo            批次号
 * @param bizOrderNo         业务单号
 * @param providerId         Provider
 * @param openid             收款 openid
 * @param amountTotalCents   总金额
 * @param amountSuccessCents 已成功金额
 * @param status             批次状态
 * @param billCount          拆单笔数
 * @param successCount       成功笔数
 * @param failCount          失败笔数
 * @param pendingCount       待转笔数
 * @param transferSceneId    场景 ID
 * @param transferRemark     备注
 * @param stopReason         停止原因
 * @param bills              明细列表
 * @param createTime         创建时间
 * @param updateTime         更新时间
 */
public record TransferBatchVO(
        String batchNo,
        String bizOrderNo,
        String providerId,
        String openid,
        Long amountTotalCents,
        Long amountSuccessCents,
        String status,
        Integer billCount,
        Integer successCount,
        Integer failCount,
        Integer pendingCount,
        String transferSceneId,
        String transferRemark,
        String stopReason,
        List<TransferBillVO> bills,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
