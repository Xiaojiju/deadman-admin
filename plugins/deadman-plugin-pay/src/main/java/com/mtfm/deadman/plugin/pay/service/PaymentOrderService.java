package com.mtfm.deadman.plugin.pay.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PaymentOrderStatus;
import com.mtfm.deadman.plugin.pay.entity.PaymentOrder;
import com.mtfm.deadman.plugin.pay.mapper.PaymentOrderMapper;
import com.mtfm.deadman.plugin.pay.spi.PaymentChannelExtra;
import com.mtfm.deadman.plugin.pay.spi.PaymentPrepayContext;
import com.mtfm.deadman.plugin.pay.spi.PaymentProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 支付平台单持久化服务，统一管理各渠道支付单的创建与状态流转。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOrderService {

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
        paymentOrderMapper.updateById(order);
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
     * 处理支付回调，更新支付单状态。
     *
     * @param outTradeNo            平台支付单号
     * @param channelTransactionId  渠道支付单号
     * @param targetStatus          目标状态
     * @param notifyRaw             回调原文
     * @return 变更前状态
     */
    @Transactional(rollbackFor = Exception.class)
    public String transitionStatus(
            String outTradeNo, String channelTransactionId, String targetStatus, String notifyRaw) {
        PaymentOrder order = requireByOutTradeNo(outTradeNo);
        String previousStatus = order.getStatus();
        if (PaymentOrderStatus.SUCCESS.equals(previousStatus) && PaymentOrderStatus.SUCCESS.equals(targetStatus)) {
            log.debug("支付单已处于 SUCCESS，忽略重复回调：{}", outTradeNo);
            return previousStatus;
        }
        order.setStatus(targetStatus);
        if (StringUtils.hasText(channelTransactionId)) {
            order.setChannelTransactionId(channelTransactionId);
        }
        if (StringUtils.hasText(notifyRaw)) {
            order.setNotifyRaw(notifyRaw);
        }
        paymentOrderMapper.updateById(order);
        return previousStatus;
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
