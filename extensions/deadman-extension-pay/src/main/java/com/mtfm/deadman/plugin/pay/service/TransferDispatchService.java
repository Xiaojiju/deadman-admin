package com.mtfm.deadman.plugin.pay.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.config.PayPluginProperties;
import com.mtfm.deadman.plugin.pay.entity.PaymentTransferBill;
import com.mtfm.deadman.plugin.pay.manager.TransferProviderManager;
import com.mtfm.deadman.plugin.pay.spi.transfer.TransferContext;
import com.mtfm.deadman.plugin.pay.spi.transfer.TransferProvider;
import com.mtfm.deadman.plugin.pay.spi.transfer.TransferQueryResult;
import com.mtfm.deadman.plugin.pay.spi.transfer.TransferResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 待转明细派发服务：按额度逐笔发起渠道转账，额度不足时停止并保留 PENDING。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferDispatchService {

    private final TransferQuotaService transferQuotaService;
    private final PaymentTransferOrderService paymentTransferOrderService;
    private final TransferProviderManager transferProviderManager;
    private final PayPluginProperties payPluginProperties;
    private final PaySensitiveCryptoHelper paySensitiveCryptoHelper;

    /**
     * 派发待转明细（受全局 dispatchEnabled 与额度约束）。
     *
     * @return 本轮成功发起渠道的笔数
     */
    public int dispatchPending() {
        if (!transferQuotaService.isDispatchEnabled()) {
            log.info("转账派发已人工停止，跳过本轮");
            return 0;
        }
        int batchSize = Math.max(1, payPluginProperties.getTransferDispatch().getBatchSize());
        List<PaymentTransferBill> pending = paymentTransferOrderService.listDispatchablePending(batchSize);
        if (pending.isEmpty()) {
            return 0;
        }
        Set<String> skippedOpenids = new HashSet<>();
        int dispatched = 0;
        for (PaymentTransferBill candidate : pending) {
            if (!transferQuotaService.isDispatchEnabled()) {
                log.info("转账派发中途被停止，已派发={}", dispatched);
                break;
            }
            if (skippedOpenids.contains(candidate.getOpenid())) {
                continue;
            }
            TransferClaimOutcome outcome =
                    paymentTransferOrderService.tryClaimPendingUnderQuota(candidate.getOutBillNo());
            if (outcome == TransferClaimOutcome.DISPATCH_DISABLED) {
                log.info("转账派发已停止，中断本轮：已派发={}", dispatched);
                break;
            }
            if (outcome == TransferClaimOutcome.GLOBAL_QUOTA_EXCEEDED) {
                log.info(
                        "全局转账额度不足，停止本轮派发：outBillNo={}, amount={}",
                        candidate.getOutBillNo(),
                        candidate.getAmountCents());
                break;
            }
            if (outcome == TransferClaimOutcome.USER_DAILY_QUOTA_EXCEEDED) {
                // 跳过该用户，继续派发其他用户待转，避免队头阻塞
                skippedOpenids.add(candidate.getOpenid());
                log.info(
                        "单用户日额度不足，跳过该用户后续单据：openid={}, outBillNo={}",
                        candidate.getOpenid(),
                        candidate.getOutBillNo());
                continue;
            }
            if (outcome != TransferClaimOutcome.CLAIMED) {
                continue;
            }
            if (invokeChannelAfterClaim(candidate.getOutBillNo())) {
                dispatched++;
            }
        }
        return dispatched;
    }

    /**
     * 派发单笔待转（含额度闸门）。
     *
     * @param outBillNo 平台转账单号
     * @return 是否成功发起渠道（不含最终成功）
     */
    public boolean dispatchOne(String outBillNo) {
        TransferClaimOutcome outcome = paymentTransferOrderService.tryClaimPendingUnderQuota(outBillNo);
        if (outcome != TransferClaimOutcome.CLAIMED) {
            return false;
        }
        return invokeChannelAfterClaim(outBillNo);
    }

    /**
     * 抢占成功后调用渠道；明确拒绝落 FAIL，不确定失败保留 PROCESSING 并查单。
     *
     * @param outBillNo 平台转账单号
     * @return 是否已向渠道发起（含不确定失败后的保留）
     */
    private boolean invokeChannelAfterClaim(String outBillNo) {
        PaymentTransferBill bill = paymentTransferOrderService.requireBill(outBillNo);
        TransferProvider provider = transferProviderManager.require(bill.getProviderId());
        TransferContext context = TransferContext.builder()
                .bizOrderNo(bill.getBizOrderNo())
                .openid(bill.getOpenid())
                .userName(paySensitiveCryptoHelper.decryptUserName(bill.getUserName()))
                .amountCents(bill.getAmountCents())
                .transferSceneId(bill.getTransferSceneId())
                .transferRemark(bill.getTransferRemark())
                .build();
        TransferResult result;
        try {
            result = provider.createTransfer(context, bill.getOutBillNo());
        } catch (BusinessException ex) {
            if (isClearChannelReject(ex)) {
                paymentTransferOrderService.markClaimedAsFailed(
                        bill.getOutBillNo(), "渠道转账申请失败：" + ex.getMessage());
                return false;
            }
            log.warn(
                    "转账渠道调用不确定失败，保留 PROCESSING 待查单：outBillNo={}, code={}",
                    bill.getOutBillNo(),
                    ex.getCode(),
                    ex);
            paymentTransferOrderService.keepProcessingAfterUncertainFailure(
                    bill.getOutBillNo(), "渠道调用异常，待查单确认");
            trySync(provider, bill.getOutBillNo());
            return true;
        } catch (RuntimeException ex) {
            log.warn("转账渠道调用异常，保留 PROCESSING 待查单：outBillNo={}", bill.getOutBillNo(), ex);
            paymentTransferOrderService.keepProcessingAfterUncertainFailure(
                    bill.getOutBillNo(), "渠道调用异常，待查单确认");
            trySync(provider, bill.getOutBillNo());
            return true;
        }
        paymentTransferOrderService.applyChannelResult(
                result.outBillNo() == null ? bill.getOutBillNo() : result.outBillNo(),
                result.channelBillNo(),
                result.targetStatus(),
                result.packageInfo(),
                bill.getAmountCents(),
                result.failReason(),
                result.rawPayload());
        if (provider.autoCompleteAfterTransfer()) {
            trySync(provider, bill.getOutBillNo());
        }
        return true;
    }

    /**
     * 微信明确业务拒绝使用 {@link ResultCode#WECHAT_PAY_TRANSFER_FAILED}；
     * 超时/网络等不确定失败使用 {@link ResultCode#PAY_TRANSFER_FAILED}。
     */
    private static boolean isClearChannelReject(BusinessException ex) {
        return ex.getCode() == ResultCode.WECHAT_PAY_TRANSFER_FAILED.getCode();
    }

    private void trySync(TransferProvider provider, String outBillNo) {
        try {
            TransferQueryResult query = provider.queryTransfer(outBillNo);
            paymentTransferOrderService.applyChannelResult(
                    query.outBillNo() == null ? outBillNo : query.outBillNo(),
                    query.channelBillNo(),
                    query.targetStatus(),
                    query.packageInfo(),
                    query.amountCents(),
                    query.failReason(),
                    query.rawPayload());
        } catch (RuntimeException ex) {
            log.warn("转账查单补偿失败：outBillNo={}", outBillNo, ex);
        }
    }
}
