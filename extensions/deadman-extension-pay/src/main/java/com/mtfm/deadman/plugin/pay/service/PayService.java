package com.mtfm.deadman.plugin.pay.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mtfm.deadman.plugin.pay.constant.PaymentOrderStatus;
import com.mtfm.deadman.plugin.pay.entity.PaymentOrder;
import com.mtfm.deadman.plugin.pay.manager.PaymentProviderManager;
import com.mtfm.deadman.plugin.pay.spi.PaymentNotifyContext;
import com.mtfm.deadman.plugin.pay.spi.PaymentNotifyResult;
import com.mtfm.deadman.plugin.pay.spi.PaymentOrderSnapshot;
import com.mtfm.deadman.plugin.pay.spi.PaymentOrderStatusChangedPublisher;
import com.mtfm.deadman.plugin.pay.spi.PaymentOutTradeNoSupplier;
import com.mtfm.deadman.plugin.pay.spi.PaymentPrepayContext;
import com.mtfm.deadman.plugin.pay.spi.PaymentPrepayResult;
import com.mtfm.deadman.plugin.pay.spi.PaymentProvider;
import com.mtfm.deadman.plugin.pay.spi.PaymentQueryResult;

import lombok.RequiredArgsConstructor;

/**
 * 支付统一门面，编排「预下单 → 写入订单 → 回调 → 事件通知 → 状态变更」完整流程。
 */
@Service
@RequiredArgsConstructor
public class PayService {

    private final PaymentProviderManager paymentProviderManager;
    private final PaymentOrderService paymentOrderService;
    private final PaymentOutTradeNoSupplier paymentOutTradeNoSupplier;
    private final PaymentOrderStatusChangedPublisher paymentOrderStatusChangedPublisher;

    /**
     * 使用默认 Provider 创建预下单。
     *
     * @param context 预下单上下文
     * @return 预下单结果
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentPrepayResult createPrepay(PaymentPrepayContext context) {
        return createPrepay(context, null);
    }

    /**
     * 指定 Provider 创建预下单：写入平台单 → 调用渠道 → 回写预支付信息。
     *
     * @param context    预下单上下文
     * @param providerId 支付 Provider 标识，为空时使用默认
     * @return 预下单结果
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentPrepayResult createPrepay(PaymentPrepayContext context, String providerId) {
        PaymentProvider provider = paymentProviderManager.require(providerId);
        String outTradeNo = paymentOutTradeNoSupplier.generate(context, provider);
        PaymentOrder order = paymentOrderService.createPendingOrder(outTradeNo, context, provider);
        PaymentPrepayResult result = provider.createPrepay(context, outTradeNo);
        paymentOrderService.updatePrepayResult(order, result.prepayId(), result.channelExtra());
        return result;
    }

    /**
     * 按平台支付单号查询支付单（从统一订单表读取）。
     *
     * @param outTradeNo 平台支付单号
     * @return 支付单快照
     */
    public PaymentOrderSnapshot queryOrder(String outTradeNo) {
        return toSnapshot(paymentOrderService.requireByOutTradeNo(outTradeNo));
    }

    /**
     * 处理支付回调：解析 → 比较状态 → 更新状态 → 发布事件。
     *
     * @param providerId 支付 Provider 标识
     * @param context    回调上下文
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleNotify(String providerId, PaymentNotifyContext context) {
        PaymentProvider provider = paymentProviderManager.require(providerId);
        PaymentNotifyResult notifyResult = provider.parseNotify(context);
        handleChannelPaymentResult(
                notifyResult.outTradeNo(),
                notifyResult.channelTransactionId(),
                notifyResult.targetStatus(),
                notifyResult.rawPayload());
    }

    /**
     * 主动向渠道查单并同步本地支付单状态，用于回调延迟或丢失时的补偿。
     * 仅当渠道状态与本地不一致时（如渠道已支付）才走与 {@link #handleNotify} 相同的状态更新与事件发布逻辑。
     *
     * @param outTradeNo 平台支付单号
     * @return 同步后的支付单快照
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentOrderSnapshot syncOrderFromChannel(String outTradeNo) {
        PaymentOrder order = paymentOrderService.requireByOutTradeNo(outTradeNo);
        if (!PaymentOrderStatus.NOT_PAY.equals(order.getStatus())) {
            return toSnapshot(order);
        }
        PaymentProvider provider = paymentProviderManager.require(order.getProviderId());
        PaymentQueryResult queryResult = provider.queryOrder(outTradeNo);
        if (queryResult.targetStatus().equals(order.getStatus())) {
            return toSnapshot(order);
        }
        return handleChannelPaymentResult(
                queryResult.outTradeNo(),
                queryResult.channelTransactionId(),
                queryResult.targetStatus(),
                queryResult.rawPayload());
    }

    /**
     * 列出已注册的支付 Provider。
     *
     * @return Provider 标识列表
     */
    public java.util.List<String> listProviders() {
        return paymentProviderManager.listProviderIds();
    }

    /**
     * 统一处理渠道支付结果：更新状态 → 发布事件（回调与主动查单共用）。
     *
     * @param outTradeNo           平台支付单号
     * @param channelTransactionId 渠道支付单号
     * @param targetStatus         目标状态
     * @param rawPayload           渠道原文
     * @return 更新后的支付单快照
     */
    private PaymentOrderSnapshot handleChannelPaymentResult(
            String outTradeNo, String channelTransactionId, String targetStatus, String rawPayload) {
        String previousStatus = paymentOrderService.transitionStatus(
                outTradeNo, channelTransactionId, targetStatus, rawPayload);
        PaymentOrder current = paymentOrderService.reload(outTradeNo);
        if (!previousStatus.equals(current.getStatus())) {
            paymentOrderStatusChangedPublisher.publish(current, previousStatus, current.getStatus());
        }
        return toSnapshot(current);
    }

    private static PaymentOrderSnapshot toSnapshot(PaymentOrder order) {
        return new PaymentOrderSnapshot(
                order.getOutTradeNo(),
                order.getBizOrderNo(),
                order.getProviderId(),
                order.getPayPlatform(),
                order.getPayMethod(),
                order.getDescription(),
                order.getAmountTotal(),
                order.getStatus(),
                order.getChannelPrepayId(),
                order.getChannelTransactionId(),
                order.getCreateTime(),
                order.getUpdateTime());
    }
}
