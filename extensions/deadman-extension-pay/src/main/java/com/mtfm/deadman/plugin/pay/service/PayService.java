package com.mtfm.deadman.plugin.pay.service;

import org.springframework.stereotype.Service;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.config.PayPluginProperties;
import com.mtfm.deadman.plugin.pay.constant.PaymentOrderStatus;
import com.mtfm.deadman.plugin.pay.entity.PaymentOrder;
import com.mtfm.deadman.plugin.pay.manager.PaymentProviderManager;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentNotifyResult;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentOrderSnapshot;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentOutTradeNoSupplier;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentPrepayContext;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentPrepayResult;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentProvider;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentQueryResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 支付统一门面，编排「预下单 → 写入订单 → 回调 → 事件通知 → 状态变更」完整流程。
 * <p>
 * 渠道 HTTP 均在事务外调用；本地落库由短事务完成（状态与事件同事务）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayService {

    private final PaymentProviderManager paymentProviderManager;
    private final PaymentOrderService paymentOrderService;
    private final PaymentChannelResultApplier paymentChannelResultApplier;
    private final PaymentOutTradeNoSupplier paymentOutTradeNoSupplier;
    private final PayPluginProperties payPluginProperties;

    /**
     * 使用默认 Provider 创建预下单。
     *
     * @param context 预下单上下文
     * @return 预下单结果
     */
    public PaymentPrepayResult createPrepay(PaymentPrepayContext context) {
        return createPrepay(context, null);
    }

    /**
     * 指定 Provider 创建预下单：短事务落单 → 渠道外呼 → 短事务回写。
     * <p>
     * 若 Provider {@link PaymentProvider#autoCompleteAfterPrepay()} 为 true（如 Mock），
     * 预下单后立即按查单结果完成支付并发布状态变更事件，无需真实回调。
     *
     * @param context    预下单上下文
     * @param providerId 支付 Provider 标识，为空时使用默认
     * @return 预下单结果
     */
    public PaymentPrepayResult createPrepay(PaymentPrepayContext context, String providerId) {
        PaymentPrepayContext effectiveContext = applyTestModeAmount(context);
        PaymentProvider provider = paymentProviderManager.require(providerId);
        String outTradeNo = paymentOutTradeNoSupplier.generate(effectiveContext, provider);
        PaymentOrder order = paymentOrderService.createPendingOrder(outTradeNo, effectiveContext, provider);
        PaymentPrepayResult result = provider.createPrepay(effectiveContext, outTradeNo);
        paymentOrderService.updatePrepayResult(order, result.prepayId(), result.channelExtra());
        if (provider.autoCompleteAfterPrepay()) {
            PaymentQueryResult queryResult = provider.queryOrder(outTradeNo);
            paymentChannelResultApplier.apply(
                    queryResult.outTradeNo(),
                    queryResult.channelTransactionId(),
                    queryResult.targetStatus(),
                    queryResult.amountTotal(),
                    queryResult.rawPayload());
        }
        return result;
    }

    /**
     * 测试模式下将预下单金额覆盖为固定小额（默认 1 分 / 0.01 元），本地支付单与渠道金额一致。
     *
     * @param context 原始预下单上下文
     * @return 可能被改写金额后的上下文
     */
    private PaymentPrepayContext applyTestModeAmount(PaymentPrepayContext context) {
        if (context == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "预下单上下文不能为空");
        }
        PayPluginProperties.TestMode testMode = payPluginProperties.getTestMode();
        if (testMode == null || !testMode.isEnabled()) {
            return context;
        }
        int fixedAmountCents = testMode.getFixedAmountCents();
        if (fixedAmountCents <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "测试模式固定支付金额必须大于 0 分");
        }
        if (context.getAmountTotal() == fixedAmountCents) {
            return context;
        }
        log.warn(
                "支付测试模式已启用：预下单金额由 {} 分覆盖为 {} 分（{} 元），bizOrderNo={}",
                context.getAmountTotal(),
                fixedAmountCents,
                String.format("%.2f", fixedAmountCents / 100.0),
                context.getBizOrderNo());
        return PaymentPrepayContext.builder()
                .bizOrderNo(context.getBizOrderNo())
                .description(context.getDescription())
                .amountTotal(fixedAmountCents)
                .payerUserId(context.getPayerUserId())
                .channelParams(context.getChannelParams())
                .build();
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
     * 处理支付回调：渠道解析（事务外）→ 短事务落库 → 发布事件。
     *
     * @param providerId 支付 Provider 标识
     * @param context    回调上下文
     */
    public void handleNotify(String providerId, ChannelNotifyContext context) {
        PaymentProvider provider = paymentProviderManager.require(providerId);
        PaymentNotifyResult notifyResult = provider.parseNotify(context);
        paymentChannelResultApplier.apply(
                notifyResult.outTradeNo(),
                notifyResult.channelTransactionId(),
                notifyResult.targetStatus(),
                notifyResult.amountTotal(),
                notifyResult.rawPayload());
    }

    /**
     * 主动向渠道查单并同步本地支付单状态，用于回调延迟或丢失时的补偿。
     * 仅当渠道状态与本地不一致时（如渠道已支付）才走与 {@link #handleNotify} 相同的状态更新与事件发布逻辑。
     *
     * @param outTradeNo 平台支付单号
     * @return 同步后的支付单快照
     */
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
        return paymentChannelResultApplier.apply(
                queryResult.outTradeNo(),
                queryResult.channelTransactionId(),
                queryResult.targetStatus(),
                queryResult.amountTotal(),
                queryResult.rawPayload());
    }

    /**
     * 按支付方式解析 Provider。
     *
     * @param paymentMethod Provider 标识或支付方式
     * @return Provider 实例
     */
    public PaymentProvider requirePaymentProvider(String paymentMethod) {
        return paymentProviderManager.requireByPaymentMethod(paymentMethod);
    }

    /**
     * 列出已注册的支付 Provider。
     *
     * @return Provider 标识列表
     */
    public java.util.List<String> listProviders() {
        return paymentProviderManager.listProviderIds();
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
                order.getAmountRefunded() == null ? 0 : order.getAmountRefunded(),
                order.getStatus(),
                order.getChannelPrepayId(),
                order.getChannelTransactionId(),
                order.getCreateTime(),
                order.getUpdateTime());
    }
}
