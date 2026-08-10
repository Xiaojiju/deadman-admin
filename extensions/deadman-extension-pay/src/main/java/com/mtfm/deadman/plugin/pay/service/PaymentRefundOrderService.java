package com.mtfm.deadman.plugin.pay.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PaymentOrderStatus;
import com.mtfm.deadman.plugin.pay.constant.PaymentRefundStatus;
import com.mtfm.deadman.plugin.pay.entity.PaymentOrder;
import com.mtfm.deadman.plugin.pay.entity.PaymentRefundOrder;
import com.mtfm.deadman.plugin.pay.mapper.PaymentRefundOrderMapper;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundContext;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 退款单持久化服务，统一管理退款单创建与状态流转。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRefundOrderService {

    /** 微信等渠道对单笔支付订单部分退款次数上限 */
    public static final int MAX_REFUNDS_PER_ORDER = 50;

    /**
     * 退款合法状态迁移表（不含同状态幂等回写）。
     * SUCCESS/CLOSED 为硬终态，不可迁出。
     */
    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            PaymentRefundStatus.PROCESSING,
            Set.of(
                    PaymentRefundStatus.SUCCESS,
                    PaymentRefundStatus.CLOSED,
                    PaymentRefundStatus.ABNORMAL,
                    PaymentRefundStatus.PROCESSING),
            PaymentRefundStatus.ABNORMAL,
            Set.of(
                    PaymentRefundStatus.PROCESSING,
                    PaymentRefundStatus.SUCCESS,
                    PaymentRefundStatus.CLOSED,
                    PaymentRefundStatus.ABNORMAL));

    private final PaymentRefundOrderMapper paymentRefundOrderMapper;
    private final PaymentOrderService paymentOrderService;

    /**
     * 在支付单行锁下校验可退余额并创建处理中退款单（短事务，不含渠道外呼）。
     *
     * @param outRefundNo 平台退款单号
     * @param outTradeNo  平台支付单号
     * @param context     退款上下文
     * @param provider    退款 Provider
     * @return 已持久化的退款单
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentRefundOrder createProcessingOrderUnderLock(
            String outRefundNo, String outTradeNo, RefundContext context, RefundProvider provider) {
        PaymentOrder payOrder = paymentOrderService.requireByOutTradeNoForUpdate(outTradeNo);
        validatePayOrderRefundable(payOrder, context.getAmountRefund());
        return createProcessingOrder(outRefundNo, payOrder, context, provider);
    }

    /**
     * 创建处理中的退款单。
     *
     * @param outRefundNo 平台退款单号
     * @param payOrder    原支付单
     * @param context     退款上下文
     * @param provider    退款 Provider
     * @return 已持久化的退款单
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentRefundOrder createProcessingOrder(
            String outRefundNo, PaymentOrder payOrder, RefundContext context, RefundProvider provider) {
        PaymentRefundOrder refundOrder = PaymentRefundOrder.builder()
                .outRefundNo(outRefundNo)
                .outTradeNo(payOrder.getOutTradeNo())
                .bizOrderNo(payOrder.getBizOrderNo())
                .amountRefund(context.getAmountRefund())
                .amountTotal(payOrder.getAmountTotal())
                .currency(StringUtils.hasText(context.getCurrency()) ? context.getCurrency() : "CNY")
                .status(PaymentRefundStatus.PROCESSING)
                .payPlatform(payOrder.getPayPlatform())
                .payMethod(payOrder.getPayMethod())
                .providerId(provider.providerId())
                .channelTransactionId(payOrder.getChannelTransactionId())
                .reason(context.getReason())
                .abnormalHandled(0)
                .build();
        paymentRefundOrderMapper.insert(refundOrder);
        return refundOrder;
    }

    /**
     * 按平台退款单号查询退款单。
     *
     * @param outRefundNo 平台退款单号
     * @return 退款单
     */
    public PaymentRefundOrder requireByOutRefundNo(String outRefundNo) {
        PaymentRefundOrder refundOrder = paymentRefundOrderMapper.selectOne(new LambdaQueryWrapper<PaymentRefundOrder>()
                .eq(PaymentRefundOrder::getOutRefundNo, outRefundNo));
        if (refundOrder == null) {
            throw new BusinessException(ResultCode.PAY_REFUND_ORDER_NOT_FOUND);
        }
        return refundOrder;
    }

    /**
     * 统计某支付单下的退款笔数（含处理中/成功/异常/关闭）。
     *
     * @param outTradeNo 平台支付单号
     * @return 退款笔数
     */
    public long countByOutTradeNo(String outTradeNo) {
        return paymentRefundOrderMapper.selectCount(new LambdaQueryWrapper<PaymentRefundOrder>()
                .eq(PaymentRefundOrder::getOutTradeNo, outTradeNo));
    }

    /**
     * 统计某支付单下未终态退款占用金额（PROCESSING + ABNORMAL）。
     *
     * @param outTradeNo 平台支付单号
     * @return 占用金额合计（分）
     */
    public int sumOutstandingAmount(String outTradeNo) {
        return paymentRefundOrderMapper.sumOutstandingAmount(outTradeNo);
    }

    /**
     * 回写渠道退款申请结果（不改变已终态单据）。
     *
     * @param outRefundNo          平台退款单号
     * @param channelRefundId      渠道退款单号
     * @param channelTransactionId 渠道支付单号
     * @param targetStatus         目标状态
     * @param userReceivedAccount  入账账户
     * @param rawPayload           原文
     * @return 状态回写结果（仅 applied=true 时可累加金额/发事件）
     */
    @Transactional(rollbackFor = Exception.class)
    public StatusTransitionResult applyChannelResult(
            String outRefundNo,
            String channelRefundId,
            String channelTransactionId,
            String targetStatus,
            String userReceivedAccount,
            String rawPayload) {
        PaymentRefundOrder refundOrder = requireByOutRefundNo(outRefundNo);
        String previousStatus = refundOrder.getStatus();
        if (!StringUtils.hasText(targetStatus)) {
            log.warn("退款目标状态为空，忽略：outRefundNo={}", outRefundNo);
            return StatusTransitionResult.notApplied(previousStatus);
        }
        String normalizedTarget = targetStatus.trim();
        if (previousStatus.equals(normalizedTarget)) {
            // 同状态仅回写渠道字段，不触发副作用
            if (StringUtils.hasText(channelRefundId)) {
                refundOrder.setChannelRefundId(channelRefundId);
            }
            if (StringUtils.hasText(channelTransactionId)) {
                refundOrder.setChannelTransactionId(channelTransactionId);
            }
            if (StringUtils.hasText(userReceivedAccount)) {
                refundOrder.setUserReceivedAccount(userReceivedAccount);
            }
            if (StringUtils.hasText(rawPayload)) {
                refundOrder.setNotifyRaw(rawPayload);
            }
            paymentRefundOrderMapper.updateById(refundOrder);
            return StatusTransitionResult.notApplied(previousStatus);
        }
        if (!isAllowedRefundTransition(previousStatus, normalizedTarget)) {
            log.warn(
                    "非法退款状态迁移，已忽略：outRefundNo={}, {} -> {}",
                    outRefundNo,
                    previousStatus,
                    normalizedTarget);
            return StatusTransitionResult.notApplied(previousStatus);
        }
        refundOrder.setStatus(normalizedTarget);
        if (StringUtils.hasText(channelRefundId)) {
            refundOrder.setChannelRefundId(channelRefundId);
        }
        if (StringUtils.hasText(channelTransactionId)) {
            refundOrder.setChannelTransactionId(channelTransactionId);
        }
        if (StringUtils.hasText(userReceivedAccount)) {
            refundOrder.setUserReceivedAccount(userReceivedAccount);
        }
        if (StringUtils.hasText(rawPayload)) {
            refundOrder.setNotifyRaw(rawPayload);
        }
        int updated = paymentRefundOrderMapper.updateById(refundOrder);
        if (updated <= 0) {
            log.warn("退款单状态更新失败（可能乐观锁冲突）：outRefundNo={}", outRefundNo);
            return StatusTransitionResult.notApplied(requireByOutRefundNo(outRefundNo).getStatus());
        }
        return StatusTransitionResult.applied(previousStatus, normalizedTarget);
    }

    /**
     * 将未获渠道受理的 PROCESSING 退款单关闭，释放可退余额占用（不发状态事件）。
     * <p>
     * 用于渠道明确拒绝申请的场景；不确定是否已受理时勿调用。
     *
     * @param outRefundNo 平台退款单号
     * @param reason      关闭原因（写入 notifyRaw）
     * @return 是否成功关闭
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean closeUnacceptedProcessingOrder(String outRefundNo, String reason) {
        PaymentRefundOrder refundOrder = requireByOutRefundNo(outRefundNo);
        if (!PaymentRefundStatus.PROCESSING.equals(refundOrder.getStatus())) {
            return false;
        }
        refundOrder.setStatus(PaymentRefundStatus.CLOSED);
        if (StringUtils.hasText(reason)) {
            refundOrder.setNotifyRaw(reason.trim());
        }
        int updated = paymentRefundOrderMapper.updateById(refundOrder);
        if (updated <= 0) {
            log.warn("关闭未受理退款单失败（可能乐观锁冲突）：outRefundNo={}", outRefundNo);
            return false;
        }
        log.info("已关闭未受理退款单：outRefundNo={}, reason={}", outRefundNo, reason);
        return true;
    }

    /**
     * CAS 抢占异常退款处理权（仅首次成功）。
     *
     * @param outRefundNo 平台退款单号
     * @return 本次成功抢占返回 true
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean tryMarkAbnormalHandled(String outRefundNo) {
        return paymentRefundOrderMapper.casMarkAbnormalHandled(outRefundNo) > 0;
    }

    /**
     * 渠道异常退款失败后清除标记，允许补偿重试。
     *
     * @param outRefundNo 平台退款单号
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearAbnormalHandled(String outRefundNo) {
        paymentRefundOrderMapper.clearAbnormalHandled(outRefundNo);
    }

    /**
     * 列出符合主动查退款条件的非终态退款单。
     *
     * @param minAge    创建后至少等待时长
     * @param maxAge    最大查单窗口
     * @param batchSize 单次扫描上限
     * @return 待同步退款单
     */
    public List<PaymentRefundOrder> listPendingForSync(Duration minAge, Duration maxAge, int batchSize) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earliestCreateTime = now.minus(maxAge);
        LocalDateTime latestEligibleCreateTime = now.minus(minAge);
        return paymentRefundOrderMapper.selectList(new LambdaQueryWrapper<PaymentRefundOrder>()
                .in(
                        PaymentRefundOrder::getStatus,
                        PaymentRefundStatus.PROCESSING,
                        PaymentRefundStatus.ABNORMAL)
                .ge(PaymentRefundOrder::getCreateTime, earliestCreateTime)
                .le(PaymentRefundOrder::getCreateTime, latestEligibleCreateTime)
                .orderByAsc(PaymentRefundOrder::getCreateTime)
                .last("LIMIT " + Math.max(batchSize, 1)));
    }

    /**
     * 重新加载退款单。
     *
     * @param outRefundNo 平台退款单号
     * @return 最新退款单
     */
    public PaymentRefundOrder reload(String outRefundNo) {
        return requireByOutRefundNo(outRefundNo);
    }

    private void validatePayOrderRefundable(PaymentOrder payOrder, int amountRefund) {
        String status = payOrder.getStatus();
        if (!PaymentOrderStatus.SUCCESS.equals(status) && !PaymentOrderStatus.REFUND.equals(status)) {
            throw new BusinessException(ResultCode.PAY_REFUND_NOT_ALLOWED, "仅已支付或已转入退款的订单可退款");
        }
        long refundCount = countByOutTradeNo(payOrder.getOutTradeNo());
        if (refundCount >= MAX_REFUNDS_PER_ORDER) {
            throw new BusinessException(
                    ResultCode.PAY_REFUND_LIMIT_EXCEEDED, "单笔订单退款次数已达上限：" + MAX_REFUNDS_PER_ORDER);
        }
        int refunded = payOrder.getAmountRefunded() == null ? 0 : payOrder.getAmountRefunded();
        int outstanding = sumOutstandingAmount(payOrder.getOutTradeNo());
        int remain = payOrder.getAmountTotal() - refunded - outstanding;
        if (amountRefund > remain) {
            throw new BusinessException(
                    ResultCode.PAY_REFUND_AMOUNT_INVALID,
                    "退款金额超出可退余额，可退：" + Math.max(remain, 0) + " 分");
        }
    }

    /**
     * 判断退款状态迁移是否合法。
     *
     * @param from 当前状态
     * @param to   目标状态
     * @return 是否允许
     */
    static boolean isAllowedRefundTransition(String from, String to) {
        if (!StringUtils.hasText(from) || !StringUtils.hasText(to)) {
            return false;
        }
        if (from.equals(to)) {
            return true;
        }
        Set<String> allowed = ALLOWED_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }
}
