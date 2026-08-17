package com.mtfm.deadman.plugin.pay.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PlatformFundBizScene;
import com.mtfm.deadman.plugin.pay.constant.TransferBatchStatus;
import com.mtfm.deadman.plugin.pay.constant.TransferBillStatus;
import com.mtfm.deadman.plugin.pay.dto.transfer.TransferBatchPageQuery;
import com.mtfm.deadman.plugin.pay.entity.PaymentTransferBatch;
import com.mtfm.deadman.plugin.pay.entity.PaymentTransferBill;
import com.mtfm.deadman.plugin.pay.entity.PaymentTransferQuota;
import com.mtfm.deadman.plugin.pay.mapper.PaymentTransferBatchMapper;
import com.mtfm.deadman.plugin.pay.mapper.PaymentTransferBillMapper;
import com.mtfm.deadman.plugin.pay.mapper.PaymentTransferQuotaMapper;
import com.mtfm.deadman.plugin.pay.util.OutTransferNoGenerator;

import lombok.RequiredArgsConstructor;

/**
 * 商家转账批次/明细持久化服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTransferOrderService {

    private final PaymentTransferBatchMapper transferBatchMapper;
    private final PaymentTransferBillMapper transferBillMapper;
    private final PaymentTransferQuotaMapper transferQuotaMapper;
    private final TransferQuotaService transferQuotaService;
    private final PlatformFundLedgerService platformFundLedgerService;

    /**
     * 拆单落库：批次 + 全部待转明细。
     *
     * @param bizOrderNo      业务单号
     * @param providerId      Provider
     * @param payPlatform     支付平台
     * @param openid          收款人
     * @param userName        姓名
     * @param amountTotal     总金额
     * @param splitAmounts    拆单金额列表
     * @param transferSceneId 场景 ID
     * @param transferRemark  备注
     * @return 批次实体
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentTransferBatch createPendingBatch(
            String bizOrderNo,
            String providerId,
            String payPlatform,
            String openid,
            String userName,
            long amountTotal,
            List<Long> splitAmounts,
            String transferSceneId,
            String transferRemark) {
        // 事务内再次查重，降低并发双插窗口
        Optional<PaymentTransferBatch> existing = findByBizOrderNo(bizOrderNo);
        if (existing.isPresent()) {
            return existing.get();
        }
        String batchNo = OutTransferNoGenerator.generateBatchNo();
        PaymentTransferBatch batch = PaymentTransferBatch.builder()
                .batchNo(batchNo)
                .bizOrderNo(bizOrderNo)
                .providerId(providerId)
                .payPlatform(payPlatform)
                .openid(openid)
                .userName(userName)
                .amountTotalCents(amountTotal)
                .amountSuccessCents(0L)
                .transferSceneId(transferSceneId)
                .transferRemark(transferRemark)
                .status(TransferBatchStatus.PENDING)
                .billCount(splitAmounts.size())
                .successCount(0)
                .failCount(0)
                .pendingCount(splitAmounts.size())
                .build();
        transferBatchMapper.insert(batch);

        int seq = 1;
        for (Long amount : splitAmounts) {
            PaymentTransferBill bill = PaymentTransferBill.builder()
                    .outBillNo(OutTransferNoGenerator.generateBillNo())
                    .batchNo(batchNo)
                    .bizOrderNo(bizOrderNo)
                    .seqNo(seq++)
                    .providerId(providerId)
                    .payPlatform(payPlatform)
                    .openid(openid)
                    .userName(userName)
                    .amountCents(amount)
                    .transferSceneId(transferSceneId)
                    .transferRemark(transferRemark)
                    .status(TransferBillStatus.PENDING)
                    .build();
            transferBillMapper.insert(bill);
        }
        return batch;
    }

    /**
     * 按业务单号查找已存在批次（幂等：同 bizOrderNo 复用原批次）。
     *
     * @param bizOrderNo 业务单号
     * @return 已存在批次
     */
    public Optional<PaymentTransferBatch> findByBizOrderNo(String bizOrderNo) {
        if (!StringUtils.hasText(bizOrderNo)) {
            return Optional.empty();
        }
        PaymentTransferBatch batch = transferBatchMapper.selectOne(new LambdaQueryWrapper<PaymentTransferBatch>()
                .eq(PaymentTransferBatch::getBizOrderNo, bizOrderNo.trim())
                .orderByAsc(PaymentTransferBatch::getId)
                .last("LIMIT 1"));
        return Optional.ofNullable(batch);
    }

    /**
     * 按批次号加载批次。
     *
     * @param batchNo 批次号
     * @return 批次
     */
    public PaymentTransferBatch requireBatch(String batchNo) {
        PaymentTransferBatch batch = transferBatchMapper.selectOne(new LambdaQueryWrapper<PaymentTransferBatch>()
                .eq(PaymentTransferBatch::getBatchNo, batchNo));
        if (batch == null) {
            throw new BusinessException(ResultCode.PAY_TRANSFER_BATCH_NOT_FOUND, "转账批次不存在：" + batchNo);
        }
        return batch;
    }

    /**
     * 分页查询转账批次。
     *
     * @param query 分页与筛选
     * @return 批次分页
     */
    public Page<PaymentTransferBatch> pageBatches(TransferBatchPageQuery query) {
        LambdaQueryWrapper<PaymentTransferBatch> wrapper = new LambdaQueryWrapper<PaymentTransferBatch>()
                .eq(StringUtils.hasText(query.getBatchNo()), PaymentTransferBatch::getBatchNo, trim(query.getBatchNo()))
                .eq(StringUtils.hasText(query.getBizOrderNo()), PaymentTransferBatch::getBizOrderNo,
                        trim(query.getBizOrderNo()))
                .eq(StringUtils.hasText(query.getStatus()), PaymentTransferBatch::getStatus, trim(query.getStatus()))
                .eq(StringUtils.hasText(query.getOpenid()), PaymentTransferBatch::getOpenid, trim(query.getOpenid()))
                .eq(StringUtils.hasText(query.getProviderId()), PaymentTransferBatch::getProviderId,
                        trim(query.getProviderId()))
                .ge(query.getCreateTimeFrom() != null, PaymentTransferBatch::getCreateTime, query.getCreateTimeFrom())
                .le(query.getCreateTimeTo() != null, PaymentTransferBatch::getCreateTime, query.getCreateTimeTo())
                .orderByDesc(PaymentTransferBatch::getCreateTime)
                .orderByDesc(PaymentTransferBatch::getId);
        return transferBatchMapper.selectPage(
                new Page<>(query.resolvedCurrent(), query.resolvedSize()), wrapper);
    }

    /**
     * 按平台转账单号加载明细。
     *
     * @param outBillNo 平台转账单号
     * @return 明细
     */
    public PaymentTransferBill requireBill(String outBillNo) {
        PaymentTransferBill bill = transferBillMapper.selectOne(new LambdaQueryWrapper<PaymentTransferBill>()
                .eq(PaymentTransferBill::getOutBillNo, outBillNo));
        if (bill == null) {
            throw new BusinessException(ResultCode.PAY_TRANSFER_BILL_NOT_FOUND, "转账单不存在：" + outBillNo);
        }
        return bill;
    }

    /**
     * 列出批次下全部明细（按序号）。
     *
     * @param batchNo 批次号
     * @return 明细列表
     */
    public List<PaymentTransferBill> listBillsByBatch(String batchNo) {
        return transferBillMapper.selectList(new LambdaQueryWrapper<PaymentTransferBill>()
                .eq(PaymentTransferBill::getBatchNo, batchNo)
                .orderByAsc(PaymentTransferBill::getSeqNo));
    }

    /**
     * 列出可派发待转（SQL 侧已排除 STOPPED 批次）。
     *
     * @param limit 最大条数
     * @return 待转明细
     */
    public List<PaymentTransferBill> listDispatchablePending(int limit) {
        List<PaymentTransferBill> pending =
                transferBillMapper.selectDispatchablePending(Math.max(1, limit));
        return pending == null ? List.of() : pending;
    }

    /**
     * 在额度行锁下原子校验额度并 CAS 抢占待转。
     * <p>
     * 串行化多实例派发的额度占用；渠道外呼必须在本事务外进行。
     *
     * @param outBillNo 平台转账单号
     * @return 抢占结果
     */
    @Transactional(rollbackFor = Exception.class)
    public TransferClaimOutcome tryClaimPendingUnderQuota(String outBillNo) {
        PaymentTransferQuota quota = transferQuotaMapper.selectCurrentForUpdate();
        if (quota == null) {
            quota = transferQuotaService.requireCurrent();
            quota = transferQuotaMapper.selectCurrentForUpdate();
        }
        if (quota == null
                || quota.getDispatchEnabled() == null
                || quota.getDispatchEnabled() != 1) {
            return TransferClaimOutcome.DISPATCH_DISABLED;
        }
        PaymentTransferBill bill = requireBill(outBillNo);
        if (!TransferBillStatus.PENDING.equals(bill.getStatus())) {
            return TransferClaimOutcome.NOT_CLAIMABLE;
        }
        PaymentTransferBatch batch = requireBatch(bill.getBatchNo());
        if (TransferBatchStatus.STOPPED.equals(batch.getStatus())) {
            return TransferClaimOutcome.NOT_CLAIMABLE;
        }
        TransferQuotaService.QuotaGate gate = transferQuotaService.evaluateQuotaGate(
                bill.getOpenid(), bill.getAmountCents(), quota);
        if (gate == TransferQuotaService.QuotaGate.USER_DAILY_EXCEEDED) {
            return TransferClaimOutcome.USER_DAILY_QUOTA_EXCEEDED;
        }
        if (gate == TransferQuotaService.QuotaGate.DAILY_EXCEEDED
                || gate == TransferQuotaService.QuotaGate.MONTHLY_EXCEEDED) {
            return TransferClaimOutcome.GLOBAL_QUOTA_EXCEEDED;
        }
        int updated = transferBillMapper.update(
                null,
                new LambdaUpdateWrapper<PaymentTransferBill>()
                        .eq(PaymentTransferBill::getId, bill.getId())
                        .eq(PaymentTransferBill::getStatus, TransferBillStatus.PENDING)
                        .eq(PaymentTransferBill::getVersion, bill.getVersion())
                        .set(PaymentTransferBill::getStatus, TransferBillStatus.PROCESSING)
                        .set(PaymentTransferBill::getDispatchedTime, LocalDateTime.now()));
        if (updated == 0) {
            return TransferClaimOutcome.NOT_CLAIMABLE;
        }
        markBatchDispatching(batch.getBatchNo());
        return TransferClaimOutcome.CLAIMED;
    }

    /**
     * 渠道明确拒绝：将抢占中的 PROCESSING 回退为 FAIL。
     *
     * @param outBillNo  平台转账单号
     * @param failReason 失败原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void markClaimedAsFailed(String outBillNo, String failReason) {
        applyChannelResult(outBillNo, null, TransferBillStatus.FAIL, null, null, failReason, null);
    }

    /**
     * 渠道结果不确定：保留 PROCESSING 交查单补偿。
     *
     * @param outBillNo 平台转账单号
     * @param note      备注写入 failReason 暂存
     */
    @Transactional(rollbackFor = Exception.class)
    public void keepProcessingAfterUncertainFailure(String outBillNo, String note) {
        PaymentTransferBill bill = requireBill(outBillNo);
        if (!TransferBillStatus.PROCESSING.equals(bill.getStatus())) {
            return;
        }
        bill.setFailReason(truncate(note, 256));
        transferBillMapper.updateById(bill);
    }

    /**
     * 回写渠道状态并刷新批次汇总。
     *
     * @param outBillNo     平台转账单号
     * @param channelBillNo 渠道单号
     * @param targetStatus  目标状态
     * @param packageInfo   package
     * @param amountCents   渠道金额（分，可选；有值时必须与本地一致）
     * @param failReason    失败原因
     * @param notifyRaw     原文
     * @return 是否实际发生状态变更
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean applyChannelResult(
            String outBillNo,
            String channelBillNo,
            String targetStatus,
            String packageInfo,
            Long amountCents,
            String failReason,
            String notifyRaw) {
        if (!StringUtils.hasText(outBillNo) || !StringUtils.hasText(targetStatus)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "转账结果缺少单号或状态");
        }
        String normalized = normalizeChannelStatus(targetStatus);
        PaymentTransferBill bill = requireBill(outBillNo);
        if (amountCents != null
                && bill.getAmountCents() != null
                && !amountCents.equals(bill.getAmountCents())) {
            log.error(
                    "转账金额与本地不一致：outBillNo={}, local={}, channel={}",
                    outBillNo,
                    bill.getAmountCents(),
                    amountCents);
            throw new BusinessException(
                    ResultCode.PAY_TRANSFER_AMOUNT_MISMATCH,
                    "转账金额与本地不一致：" + outBillNo);
        }
        String previousStatus = bill.getStatus();
        if (TransferBillStatus.isTerminal(bill.getStatus()) && bill.getStatus().equals(normalized)) {
            return false;
        }
        if (TransferBillStatus.isTerminal(bill.getStatus()) && !bill.getStatus().equals(normalized)) {
            // 硬终态冲突：禁止静默忽略，抛错以便回调 ACK 失败并触发告警/重试
            log.error(
                    "CRITICAL 转账硬终态冲突：outBillNo={}, localStatus={}, channelStatus={}",
                    outBillNo,
                    bill.getStatus(),
                    normalized);
            throw new BusinessException(
                    ResultCode.PAY_TRANSFER_STATUS_CONFLICT,
                    "转账本地终态与渠道结果冲突：" + outBillNo + " local=" + bill.getStatus()
                            + " channel=" + normalized);
        }
        // 禁止已派发单据回退 PENDING，避免二次 createTransfer
        if (TransferBillStatus.PENDING.equals(normalized)
                && !TransferBillStatus.PENDING.equals(bill.getStatus())) {
            return false;
        }
        if (StringUtils.hasText(channelBillNo)) {
            bill.setChannelBillNo(channelBillNo);
        }
        if (StringUtils.hasText(packageInfo)) {
            bill.setPackageInfo(packageInfo);
        }
        if (StringUtils.hasText(failReason)) {
            bill.setFailReason(truncate(failReason, 256));
        }
        if (StringUtils.hasText(notifyRaw)) {
            bill.setNotifyRaw(notifyRaw);
        }
        bill.setStatus(normalized);
        if (TransferBillStatus.isTerminal(normalized)) {
            bill.setFinishedTime(LocalDateTime.now());
        }
        if (bill.getDispatchedTime() == null
                && (TransferBillStatus.countsTowardQuota(normalized)
                        || TransferBillStatus.FAIL.equals(normalized))) {
            bill.setDispatchedTime(LocalDateTime.now());
        }
        transferBillMapper.updateById(bill);
        refreshBatchAggregates(bill.getBatchNo());
        if (TransferBillStatus.SUCCESS.equals(normalized)
                && !TransferBillStatus.SUCCESS.equals(previousStatus)
                && bill.getAmountCents() != null
                && bill.getAmountCents() > 0) {
            // 运营账户出账：与业务侧 platform_partner_fund_flow 并存，幂等键按明细单号
            platformFundLedgerService.record(
                    PlatformFundBizScene.PAYOUT_TO_WALLET,
                    bill.getAmountCents(),
                    bill.getPayPlatform(),
                    bill.getBizOrderNo(),
                    firstNonBlank(channelBillNo, bill.getChannelBillNo()),
                    "商家转账到零钱",
                    "pf_payout_bill:" + bill.getOutBillNo());
        }
        return true;
    }

    /**
     * 停止批次派发（待转明细保留，调度跳过）。
     *
     * @param batchNo    批次号
     * @param stopReason 停止原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void stopBatch(String batchNo, String stopReason) {
        PaymentTransferBatch batch = requireBatch(batchNo);
        if (TransferBatchStatus.SUCCESS.equals(batch.getStatus())
                || TransferBatchStatus.FAILED.equals(batch.getStatus())) {
            return;
        }
        batch.setStatus(TransferBatchStatus.STOPPED);
        batch.setStopReason(truncate(stopReason, 256));
        transferBatchMapper.updateById(batch);
    }

    /**
     * 恢复已停止批次（若仍有待转则回到 PENDING/DISPATCHING）。
     *
     * @param batchNo 批次号
     */
    @Transactional(rollbackFor = Exception.class)
    public void resumeBatch(String batchNo) {
        PaymentTransferBatch batch = requireBatch(batchNo);
        if (!TransferBatchStatus.STOPPED.equals(batch.getStatus())) {
            return;
        }
        batch.setStopReason(null);
        transferBatchMapper.updateById(batch);
        refreshBatchAggregates(batchNo);
    }

    /**
     * 列出非终态明细供主动查单。
     *
     * @param minAge    最小年龄
     * @param maxAge    最大年龄
     * @param batchSize 批量
     * @return 明细列表
     */
    public List<PaymentTransferBill> listNonTerminalForSync(
            java.time.Duration minAge, java.time.Duration maxAge, int batchSize) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime latest = now.minus(minAge);
        LocalDateTime earliest = now.minus(maxAge);
        // 以派发时间为主，避免 PENDING 排队过久后 create_time 超出窗口导致永不查单
        return transferBillMapper.selectList(new LambdaQueryWrapper<PaymentTransferBill>()
                .in(
                        PaymentTransferBill::getStatus,
                        TransferBillStatus.ACCEPTED,
                        TransferBillStatus.PROCESSING,
                        TransferBillStatus.WAIT_USER_CONFIRM,
                        TransferBillStatus.TRANSFERING)
                .apply(
                        "COALESCE(dispatched_time, create_time) >= {0} AND COALESCE(dispatched_time, create_time) <= {1}",
                        earliest,
                        latest)
                .orderByAsc(PaymentTransferBill::getUpdateTime)
                .last("LIMIT " + Math.max(1, batchSize)));
    }

    private void markBatchDispatching(String batchNo) {
        PaymentTransferBatch batch = requireBatch(batchNo);
        if (TransferBatchStatus.PENDING.equals(batch.getStatus())
                || TransferBatchStatus.PARTIAL_SUCCESS.equals(batch.getStatus())) {
            batch.setStatus(TransferBatchStatus.DISPATCHING);
            transferBatchMapper.updateById(batch);
        }
    }

    private void refreshBatchAggregates(String batchNo) {
        PaymentTransferBatch batch = requireBatch(batchNo);
        List<PaymentTransferBill> bills = listBillsByBatch(batchNo);
        int success = 0;
        int fail = 0;
        int pending = 0;
        int inFlight = 0;
        long successAmount = 0L;
        for (PaymentTransferBill bill : bills) {
            if (TransferBillStatus.SUCCESS.equals(bill.getStatus())) {
                success++;
                successAmount += bill.getAmountCents() == null ? 0L : bill.getAmountCents();
            } else if (TransferBillStatus.FAIL.equals(bill.getStatus())
                    || TransferBillStatus.CANCELLED.equals(bill.getStatus())) {
                fail++;
            } else if (TransferBillStatus.PENDING.equals(bill.getStatus())) {
                pending++;
            } else {
                inFlight++;
            }
        }
        batch.setSuccessCount(success);
        batch.setFailCount(fail);
        batch.setPendingCount(pending);
        batch.setAmountSuccessCents(successAmount);
        if (TransferBatchStatus.STOPPED.equals(batch.getStatus()) && pending > 0) {
            // 保持 STOPPED，仅刷新计数
            transferBatchMapper.updateById(batch);
            return;
        }
        if (pending == 0 && inFlight == 0) {
            if (success == bills.size()) {
                batch.setStatus(TransferBatchStatus.SUCCESS);
            } else if (fail == bills.size()) {
                batch.setStatus(TransferBatchStatus.FAILED);
            } else if (success > 0) {
                batch.setStatus(TransferBatchStatus.PARTIAL_SUCCESS);
            } else {
                batch.setStatus(TransferBatchStatus.FAILED);
            }
        } else if (inFlight > 0 || success > 0 || fail > 0) {
            if (success > 0 && pending > 0) {
                batch.setStatus(TransferBatchStatus.PARTIAL_SUCCESS);
            } else {
                batch.setStatus(TransferBatchStatus.DISPATCHING);
            }
        } else {
            batch.setStatus(TransferBatchStatus.PENDING);
        }
        transferBatchMapper.updateById(batch);
    }

    private static String normalizeChannelStatus(String status) {
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "PENDING" -> TransferBillStatus.PENDING;
            case "ACCEPTED" -> TransferBillStatus.ACCEPTED;
            case "PROCESSING" -> TransferBillStatus.PROCESSING;
            case "WAIT_USER_CONFIRM" -> TransferBillStatus.WAIT_USER_CONFIRM;
            case "TRANSFERING", "TRANSFERRING" -> TransferBillStatus.TRANSFERING;
            case "SUCCESS" -> TransferBillStatus.SUCCESS;
            case "FAIL", "FAILED" -> TransferBillStatus.FAIL;
            case "CANCELLED", "CANCELED", "CANCELING", "CANCELLING" -> TransferBillStatus.CANCELLED;
            default -> throw new BusinessException(ResultCode.PAY_TRANSFER_NOTIFY_PARSE_FAILED, "未知转账状态：" + status);
        };
    }

    private static String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first.trim();
        }
        return StringUtils.hasText(second) ? second.trim() : null;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String truncate(String value, int max) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
