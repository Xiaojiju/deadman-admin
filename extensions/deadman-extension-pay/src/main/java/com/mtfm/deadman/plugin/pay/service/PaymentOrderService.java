package com.mtfm.deadman.plugin.pay.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PaymentOrderStatus;
import com.mtfm.deadman.plugin.pay.entity.PaymentOrder;
import com.mtfm.deadman.plugin.pay.mapper.PaymentOrderMapper;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentChannelExtra;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentPrepayContext;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 支付平台单持久化服务，统一管理各渠道支付单的创建与状态流转。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOrderService {

    /** 支付回调/查单硬终态：不可再被渠道结果改写为其他状态 */
    private static final Set<String> HARD_TERMINAL_STATUSES =
            Set.of(PaymentOrderStatus.SUCCESS, PaymentOrderStatus.CLOSED, PaymentOrderStatus.REFUND);

    private final PaymentOrderMapper paymentOrderMapper;

    /**
     * 创建待支付平台单。
     *
     * @param outTradeNo 平台支付单号
     * @param context    预下单上下文
     * @param provider   支付 Provider
     * @return 已持久化的支付单
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentOrder createPendingOrder(String outTradeNo, PaymentPrepayContext context, PaymentProvider provider) {
        PaymentOrder order = PaymentOrder.builder()
                .outTradeNo(outTradeNo)
                .bizOrderNo(context.getBizOrderNo())
                .description(context.getDescription())
                .amountTotal(context.getAmountTotal())
                .amountRefunded(0)
                .status(PaymentOrderStatus.NOT_PAY)
                .payPlatform(provider.payPlatform())
                .payMethod(provider.payMethod())
                .providerId(provider.providerId())
                .payerUserId(context.getPayerUserId())
                .build();
        paymentOrderMapper.insert(order);
        return order;
    }

    /**
     * 退款成功后原子累加已退金额，并将支付单状态置为 {@link PaymentOrderStatus#REFUND}。
     *
     * @param outTradeNo           平台支付单号
     * @param refundAmount         本次成功退款金额（分）
     * @param channelTransactionId 渠道支付单号（可选回写）
     */
    @Transactional(rollbackFor = Exception.class)
    public void applySuccessfulRefund(String outTradeNo, int refundAmount, String channelTransactionId) {
        String txId = StringUtils.hasText(channelTransactionId) ? channelTransactionId.trim() : null;
        int updated = paymentOrderMapper.addRefundedAmount(
                outTradeNo, refundAmount, PaymentOrderStatus.REFUND, txId);
        if (updated <= 0) {
            throw new BusinessException(
                    ResultCode.PAY_REFUND_AMOUNT_INVALID,
                    "累加退款金额失败（支付单不存在或已超过可退总额）：" + outTradeNo);
        }
    }

    /**
     * 预下单成功后更新渠道预支付信息。
     *
     * @param order           支付单
     * @param channelPrepayId 渠道预支付 ID
     * @param channelExtra    渠道扩展信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePrepayResult(PaymentOrder order, String channelPrepayId, PaymentChannelExtra channelExtra) {
        order.setChannelPrepayId(channelPrepayId);
        order.setChannelExtra(toChannelExtraJson(channelExtra));
        int updated = paymentOrderMapper.updateById(order);
        if (updated <= 0) {
            log.warn("回写预支付信息失败（可能乐观锁冲突）：outTradeNo={}", order.getOutTradeNo());
        }
    }

    /**
     * 按平台支付单号查询支付单。
     *
     * @param outTradeNo 平台支付单号
     * @return 支付单
     */
    public PaymentOrder requireByOutTradeNo(String outTradeNo) {
        PaymentOrder order = paymentOrderMapper.selectOne(new LambdaQueryWrapper<PaymentOrder>()
                .eq(PaymentOrder::getOutTradeNo, outTradeNo));
        if (order == null) {
            throw new BusinessException(ResultCode.PAY_ORDER_NOT_FOUND);
        }
        return order;
    }

    /**
     * 按平台支付单号加行锁查询（用于退款并发控制，须在事务内调用）。
     *
     * @param outTradeNo 平台支付单号
     * @return 支付单
     */
    public PaymentOrder requireByOutTradeNoForUpdate(String outTradeNo) {
        PaymentOrder order = paymentOrderMapper.selectOne(new LambdaQueryWrapper<PaymentOrder>()
                .eq(PaymentOrder::getOutTradeNo, outTradeNo)
                .last("FOR UPDATE"));
        if (order == null) {
            throw new BusinessException(ResultCode.PAY_ORDER_NOT_FOUND);
        }
        return order;
    }

    /**
     * 按业务订单号查询最近一笔支付平台单。
     *
     * @param bizOrderNo 业务订单号
     * @return 支付单，不存在时返回 null
     */
    public PaymentOrder findLatestByBizOrderNo(String bizOrderNo) {
        if (!StringUtils.hasText(bizOrderNo)) {
            return null;
        }
        return paymentOrderMapper.selectOne(new LambdaQueryWrapper<PaymentOrder>()
                .eq(PaymentOrder::getBizOrderNo, bizOrderNo.trim())
                .orderByDesc(PaymentOrder::getCreateTime)
                .orderByDesc(PaymentOrder::getId)
                .last("LIMIT 1"));
    }

    /**
     * 按业务订单号查询最近一笔可退款支付单（已支付或已转入退款）。
     *
     * @param bizOrderNo 业务订单号
     * @return 可退款支付单，不存在时返回 null
     */
    public PaymentOrder findLatestRefundableByBizOrderNo(String bizOrderNo) {
        if (!StringUtils.hasText(bizOrderNo)) {
            return null;
        }
        return paymentOrderMapper.selectOne(new LambdaQueryWrapper<PaymentOrder>()
                .eq(PaymentOrder::getBizOrderNo, bizOrderNo.trim())
                .in(PaymentOrder::getStatus, PaymentOrderStatus.SUCCESS, PaymentOrderStatus.REFUND)
                .orderByDesc(PaymentOrder::getCreateTime)
                .orderByDesc(PaymentOrder::getId)
                .last("LIMIT 1"));
    }

    /**
     * 处理支付回调/查单结果，按合法状态机更新支付单。
     *
     * @param outTradeNo           平台支付单号
     * @param channelTransactionId 渠道支付单号
     * @param targetStatus         目标状态
     * @param notifyRaw            回调原文
     * @return 状态回写结果（仅 applied=true 时可发事件）
     */
    @Transactional(rollbackFor = Exception.class)
    public StatusTransitionResult transitionStatus(
            String outTradeNo, String channelTransactionId, String targetStatus, String notifyRaw) {
        PaymentOrder order = requireByOutTradeNo(outTradeNo);
        String previousStatus = order.getStatus();
        if (previousStatus.equals(targetStatus)) {
            if (StringUtils.hasText(channelTransactionId) || StringUtils.hasText(notifyRaw)) {
                if (StringUtils.hasText(channelTransactionId)) {
                    order.setChannelTransactionId(channelTransactionId);
                }
                if (StringUtils.hasText(notifyRaw)) {
                    order.setNotifyRaw(notifyRaw);
                }
                paymentOrderMapper.updateById(order);
            } else {
                log.debug("支付单状态未变，忽略：outTradeNo={}, status={}", outTradeNo, previousStatus);
            }
            return StatusTransitionResult.notApplied(previousStatus);
        }
        if (!isAllowedPaymentTransition(previousStatus, targetStatus)) {
            log.warn(
                    "非法支付状态迁移，已忽略：outTradeNo={}, {} -> {}",
                    outTradeNo,
                    previousStatus,
                    targetStatus);
            return StatusTransitionResult.notApplied(previousStatus);
        }
        order.setStatus(targetStatus);
        if (StringUtils.hasText(channelTransactionId)) {
            order.setChannelTransactionId(channelTransactionId);
        }
        if (StringUtils.hasText(notifyRaw)) {
            order.setNotifyRaw(notifyRaw);
        }
        int updated = paymentOrderMapper.updateById(order);
        if (updated <= 0) {
            log.warn("支付单状态更新失败（可能乐观锁冲突）：outTradeNo={}", outTradeNo);
            return StatusTransitionResult.notApplied(requireByOutTradeNo(outTradeNo).getStatus());
        }
        return StatusTransitionResult.applied(previousStatus, targetStatus);
    }

    /**
     * 在状态流转完成后重新加载最新支付单。
     *
     * @param outTradeNo 平台支付单号
     * @return 最新支付单
     */
    public PaymentOrder reload(String outTradeNo) {
        return requireByOutTradeNo(outTradeNo);
    }

    /**
     * 列出符合主动查单条件的待支付单。
     * 条件：状态为 NOT_PAY，且创建时间在 [now-maxAge, now-minAge] 区间内。
     *
     * @param minAge    预下单后至少等待时长
     * @param maxAge    待支付单最大查单窗口
     * @param batchSize 单次扫描上限
     * @return 待查单支付单列表
     */
    public List<PaymentOrder> listPendingForSync(Duration minAge, Duration maxAge, int batchSize) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earliestCreateTime = now.minus(maxAge);
        LocalDateTime latestEligibleCreateTime = now.minus(minAge);
        return paymentOrderMapper.selectList(new LambdaQueryWrapper<PaymentOrder>()
                .eq(PaymentOrder::getStatus, PaymentOrderStatus.NOT_PAY)
                .ge(PaymentOrder::getCreateTime, earliestCreateTime)
                .le(PaymentOrder::getCreateTime, latestEligibleCreateTime)
                .orderByAsc(PaymentOrder::getCreateTime)
                .last("LIMIT " + Math.max(batchSize, 1)));
    }

    /**
     * 支付回调/查单合法状态迁移：仅 NOT_PAY → SUCCESS/CLOSED；硬终态不可回退。
     *
     * @param from 当前状态
     * @param to   目标状态
     * @return 是否允许
     */
    static boolean isAllowedPaymentTransition(String from, String to) {
        if (!StringUtils.hasText(from) || !StringUtils.hasText(to)) {
            return false;
        }
        if (from.equals(to)) {
            return true;
        }
        if (HARD_TERMINAL_STATUSES.contains(from)) {
            return false;
        }
        if (PaymentOrderStatus.NOT_PAY.equals(from)) {
            return PaymentOrderStatus.SUCCESS.equals(to) || PaymentOrderStatus.CLOSED.equals(to);
        }
        return false;
    }

    private String toChannelExtraJson(PaymentChannelExtra channelExtra) {
        if (channelExtra == null || !StringUtils.hasText(channelExtra.openid())) {
            return null;
        }
        return "{\"openid\":\"" + escapeJson(channelExtra.openid()) + "\"}";
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
