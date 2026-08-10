package com.mtfm.deadman.plugin.pay.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.config.PayPluginProperties;
import com.mtfm.deadman.plugin.pay.constant.AbnormalRefundReceiveType;
import com.mtfm.deadman.plugin.pay.constant.PaymentRefundStatus;
import com.mtfm.deadman.plugin.pay.entity.PaymentOrder;
import com.mtfm.deadman.plugin.pay.entity.PaymentRefundOrder;
import com.mtfm.deadman.plugin.pay.manager.RefundProviderManager;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.spi.refund.AbnormalRefundContext;
import com.mtfm.deadman.plugin.pay.spi.refund.OutRefundNoSupplier;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundContext;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundNotifyResult;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundOrderSnapshot;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundProvider;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundQueryResult;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 退款统一门面，编排「校验支付单 → 写退款单 → 调渠道 → 回调/查单 → 事件通知」。
 * <p>
 * 渠道 HTTP 均在事务外调用；本地落库由短事务完成。异常退款由 AFTER_COMMIT 异步监听触发。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundProviderManager refundProviderManager;
    private final PaymentOrderService paymentOrderService;
    private final PaymentRefundOrderService paymentRefundOrderService;
    private final RefundChannelResultApplier refundChannelResultApplier;
    private final OutRefundNoSupplier outRefundNoSupplier;
    private final PayPluginProperties payPluginProperties;

    /**
     * 发起退款：短事务加锁落单 → 渠道外呼 → 短事务回写。
     * <p>
     * 支持部分退款与多次退款；同一支付单最多
     * {@link PaymentRefundOrderService#MAX_REFUNDS_PER_ORDER} 次。
     *
     * @param request 退款请求（至少包含 outTradeNo 与 amountRefund）
     * @return 退款申请结果快照
     */
    public RefundOrderSnapshot createRefund(RefundContext request) {
        if (request == null || !StringUtils.hasText(request.getOutTradeNo())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "退款缺少平台支付单号");
        }
        if (request.getAmountRefund() <= 0) {
            throw new BusinessException(ResultCode.PAY_REFUND_AMOUNT_INVALID, "退款金额必须大于 0");
        }
        PaymentOrder payOrder = paymentOrderService.requireByOutTradeNo(request.getOutTradeNo().trim());
        RefundProvider provider = refundProviderManager.require(payOrder.getProviderId());
        int amountRefund = resolveRefundAmountForTestMode(payOrder, request.getAmountRefund());
        RefundContext context = RefundContext.builder()
                .outTradeNo(payOrder.getOutTradeNo())
                .channelTransactionId(firstNonBlank(
                        request.getChannelTransactionId(), payOrder.getChannelTransactionId()))
                .bizOrderNo(payOrder.getBizOrderNo())
                .amountRefund(amountRefund)
                .amountTotal(payOrder.getAmountTotal())
                .currency(StringUtils.hasText(request.getCurrency()) ? request.getCurrency() : "CNY")
                .reason(request.getReason())
                .channelParams(request.getChannelParams())
                .build();

        String outRefundNo = outRefundNoSupplier.generate(context, provider);
        PaymentRefundOrder refundOrder = paymentRefundOrderService.createProcessingOrderUnderLock(
                outRefundNo, payOrder.getOutTradeNo(), context, provider);

        RefundResult result;
        try {
            result = provider.createRefund(context, outRefundNo);
        } catch (BusinessException ex) {
            if (isClearRefundReject(ex)) {
                // 渠道明确拒绝：关闭本地单释放 outstanding，不发业务事件
                paymentRefundOrderService.closeUnacceptedProcessingOrder(
                        outRefundNo, "渠道退款申请失败：" + ex.getMessage());
                throw ex;
            }
            // 不确定是否已受理：保留 PROCESSING，尝试查单
            tryRecoverAfterCreateFailure(outRefundNo, provider);
            throw ex;
        } catch (RuntimeException ex) {
            // 超时/网络等不确定是否已受理：尝试查单同步；仍失败则保留 PROCESSING 交补偿任务
            tryRecoverAfterCreateFailure(outRefundNo, provider);
            throw new BusinessException(ResultCode.PAY_REFUND_FAILED, "退款申请异常，请稍后查询退款结果", ex);
        }
        applyChannelRefundResult(
                result.outRefundNo(),
                result.outTradeNo(),
                result.channelRefundId(),
                result.channelTransactionId(),
                result.targetStatus(),
                result.userReceivedAccount(),
                result.amountRefund() == null ? null : result.amountRefund(),
                result.rawPayload());

        if (provider.autoCompleteAfterRefund()) {
            RefundQueryResult queryResult = provider.queryRefund(outRefundNo);
            applyChannelRefundResult(
                    queryResult.outRefundNo(),
                    queryResult.outTradeNo(),
                    queryResult.channelRefundId(),
                    queryResult.channelTransactionId(),
                    queryResult.targetStatus(),
                    queryResult.userReceivedAccount(),
                    queryResult.amountRefund(),
                    queryResult.rawPayload());
        }
        return toSnapshot(paymentRefundOrderService.reload(refundOrder.getOutRefundNo()));
    }

    /**
     * 按平台退款单号查询本地退款单。
     *
     * @param outRefundNo 平台退款单号
     * @return 退款单快照
     */
    public RefundOrderSnapshot queryRefund(String outRefundNo) {
        return toSnapshot(paymentRefundOrderService.requireByOutRefundNo(outRefundNo));
    }

    /**
     * 处理退款回调：渠道解析（事务外）→ 短事务落库 → 发布事件。
     *
     * @param providerId 退款 Provider 标识
     * @param context    回调上下文
     */
    public void handleRefundNotify(String providerId, ChannelNotifyContext context) {
        RefundProvider provider = refundProviderManager.require(providerId);
        RefundNotifyResult notifyResult = provider.parseRefundNotify(context);
        applyChannelRefundResult(
                notifyResult.outRefundNo(),
                notifyResult.outTradeNo(),
                notifyResult.channelRefundId(),
                notifyResult.channelTransactionId(),
                notifyResult.targetStatus(),
                notifyResult.userReceivedAccount(),
                notifyResult.amountRefund(),
                notifyResult.rawPayload());
    }

    /**
     * 主动向渠道查退款并同步本地状态。
     *
     * @param outRefundNo 平台退款单号
     * @return 同步后的退款单快照
     */
    public RefundOrderSnapshot syncRefundFromChannel(String outRefundNo) {
        PaymentRefundOrder refundOrder = paymentRefundOrderService.requireByOutRefundNo(outRefundNo);
        if (PaymentRefundStatus.SUCCESS.equals(refundOrder.getStatus())
                || PaymentRefundStatus.CLOSED.equals(refundOrder.getStatus())) {
            return toSnapshot(refundOrder);
        }
        RefundProvider provider = refundProviderManager.require(refundOrder.getProviderId());
        RefundQueryResult queryResult = provider.queryRefund(outRefundNo);
        if (queryResult.targetStatus().equals(refundOrder.getStatus())) {
            return toSnapshot(refundOrder);
        }
        return applyChannelRefundResult(
                queryResult.outRefundNo(),
                queryResult.outTradeNo(),
                queryResult.channelRefundId(),
                queryResult.channelTransactionId(),
                queryResult.targetStatus(),
                queryResult.userReceivedAccount(),
                queryResult.amountRefund(),
                queryResult.rawPayload());
    }

    /**
     * 对处于 ABNORMAL 的退款单发起异常退款（渠道外呼在事务外）。
     *
     * @param context 异常退款上下文（至少包含 outRefundNo；未填入账方式时使用配置默认值）
     * @return 处理后的退款单快照
     */
    public RefundOrderSnapshot createAbnormalRefund(AbnormalRefundContext context) {
        PreparedAbnormalRefund prepared = prepareAbnormalRefund(context);
        if (!paymentRefundOrderService.tryMarkAbnormalHandled(prepared.outRefundNo())) {
            log.info("异常退款已处理或处理中，跳过渠道调用：outRefundNo={}", prepared.outRefundNo());
            return toSnapshot(paymentRefundOrderService.requireByOutRefundNo(prepared.outRefundNo()));
        }
        try {
            RefundResult result = prepared.provider().createAbnormalRefund(prepared.channelContext());
            return applyChannelRefundResult(
                    result.outRefundNo(),
                    result.outTradeNo(),
                    result.channelRefundId(),
                    result.channelTransactionId(),
                    result.targetStatus(),
                    resolveAbnormalReceiveAccount(
                            prepared.channelContext().getReceiveType(), result.userReceivedAccount()),
                    result.amountRefund(),
                    result.rawPayload());
        } catch (RuntimeException ex) {
            paymentRefundOrderService.clearAbnormalHandled(prepared.outRefundNo());
            throw ex;
        }
    }

    /**
     * 退款变为 ABNORMAL 后的自动异常退款（由 AFTER_COMMIT 异步监听调用）。
     * <p>
     * CAS 抢占处理标记；渠道失败时清除标记以便补偿重试。
     *
     * @param outRefundNo 平台退款单号
     */
    public void autoHandleAbnormalRefund(String outRefundNo) {
        if (!payPluginProperties.getAbnormalRefund().isAutoEnabled()) {
            log.info("异常退款自动处理已关闭，跳过：outRefundNo={}", outRefundNo);
            return;
        }
        if (!StringUtils.hasText(outRefundNo)) {
            return;
        }
        String refundNo = outRefundNo.trim();
        PaymentRefundOrder refundOrder = paymentRefundOrderService.requireByOutRefundNo(refundNo);
        if (!PaymentRefundStatus.ABNORMAL.equals(refundOrder.getStatus())) {
            return;
        }
        if (!paymentRefundOrderService.tryMarkAbnormalHandled(refundNo)) {
            log.info("异常退款已处理或处理中，跳过：outRefundNo={}", refundNo);
            return;
        }
        try {
            PreparedAbnormalRefund prepared = prepareAbnormalRefund(AbnormalRefundContext.builder()
                    .outRefundNo(refundOrder.getOutRefundNo())
                    .channelRefundId(refundOrder.getChannelRefundId())
                    .receiveType(resolveDefaultReceiveType())
                    .build());
            RefundResult result = prepared.provider().createAbnormalRefund(prepared.channelContext());
            applyChannelRefundResult(
                    result.outRefundNo(),
                    result.outTradeNo(),
                    result.channelRefundId(),
                    result.channelTransactionId(),
                    result.targetStatus(),
                    resolveAbnormalReceiveAccount(
                            prepared.channelContext().getReceiveType(), result.userReceivedAccount()),
                    result.amountRefund(),
                    result.rawPayload());
            log.info(
                    "已自动发起异常退款：outRefundNo={}, receiveType={}",
                    refundNo,
                    prepared.channelContext().getReceiveType());
        } catch (RuntimeException ex) {
            paymentRefundOrderService.clearAbnormalHandled(refundNo);
            log.warn("自动发起异常退款失败，已清除处理标记以便重试：outRefundNo={}", refundNo, ex);
        }
    }

    private PreparedAbnormalRefund prepareAbnormalRefund(AbnormalRefundContext context) {
        if (context == null || !StringUtils.hasText(context.getOutRefundNo())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "异常退款缺少平台退款单号");
        }
        PaymentRefundOrder refundOrder =
                paymentRefundOrderService.requireByOutRefundNo(context.getOutRefundNo().trim());
        if (!PaymentRefundStatus.ABNORMAL.equals(refundOrder.getStatus())) {
            throw new BusinessException(ResultCode.PAY_ABNORMAL_REFUND_NOT_ALLOWED, "仅 ABNORMAL 状态可发起异常退款");
        }
        String channelRefundId = firstNonBlank(context.getChannelRefundId(), refundOrder.getChannelRefundId());
        if (!StringUtils.hasText(channelRefundId)) {
            throw new BusinessException(ResultCode.PAY_ABNORMAL_REFUND_FAILED, "缺少渠道退款单号，无法发起异常退款");
        }
        String receiveType = StringUtils.hasText(context.getReceiveType())
                ? context.getReceiveType().trim()
                : resolveDefaultReceiveType();
        if (AbnormalRefundReceiveType.USER_BANK_CARD.equals(receiveType)
                && (!StringUtils.hasText(context.getBankType())
                        || !StringUtils.hasText(context.getBankAccount())
                        || !StringUtils.hasText(context.getRealName()))) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "退至用户银行卡时须提供 bankType/bankAccount/realName");
        }
        RefundProvider provider = refundProviderManager.require(refundOrder.getProviderId());
        AbnormalRefundContext channelContext = AbnormalRefundContext.builder()
                .outRefundNo(refundOrder.getOutRefundNo())
                .channelRefundId(channelRefundId)
                .receiveType(receiveType)
                .bankType(context.getBankType())
                .bankAccount(context.getBankAccount())
                .realName(context.getRealName())
                .build();
        return new PreparedAbnormalRefund(refundOrder.getOutRefundNo(), provider, channelContext);
    }

    private String resolveDefaultReceiveType() {
        String configured = payPluginProperties.getAbnormalRefund().getDefaultReceiveType();
        if (AbnormalRefundReceiveType.USER_BANK_CARD.equalsIgnoreCase(configured)) {
            return AbnormalRefundReceiveType.USER_BANK_CARD;
        }
        return AbnormalRefundReceiveType.MERCHANT_BANK_CARD;
    }

    /**
     * 测试模式下支付单金额可能被压成小额，将退款申请金额限制在支付单可退余额内。
     *
     * @param payOrder      支付单
     * @param amountRefund  申请退款金额（分）
     * @return 实际用于渠道退款的金额
     */
    private int resolveRefundAmountForTestMode(PaymentOrder payOrder, int amountRefund) {
        PayPluginProperties.TestMode testMode = payPluginProperties.getTestMode();
        if (testMode == null || !testMode.isEnabled()) {
            return amountRefund;
        }
        int refunded = payOrder.getAmountRefunded() == null ? 0 : payOrder.getAmountRefunded();
        int outstanding = paymentRefundOrderService.sumOutstandingAmount(payOrder.getOutTradeNo());
        int remain = Math.max(payOrder.getAmountTotal() - refunded - outstanding, 0);
        if (amountRefund <= remain) {
            return amountRefund;
        }
        if (remain <= 0) {
            return amountRefund;
        }
        log.warn(
                "支付测试模式已启用：退款金额由 {} 分调整为支付单可退余额 {} 分，outTradeNo={}",
                amountRefund,
                remain,
                payOrder.getOutTradeNo());
        return remain;
    }

    /**
     * 退款申请异常后尝试向渠道查单并回写；查不到则保留 PROCESSING。
     */
    private void tryRecoverAfterCreateFailure(String outRefundNo, RefundProvider provider) {
        try {
            RefundQueryResult queryResult = provider.queryRefund(outRefundNo);
            applyChannelRefundResult(
                    queryResult.outRefundNo(),
                    queryResult.outTradeNo(),
                    queryResult.channelRefundId(),
                    queryResult.channelTransactionId(),
                    queryResult.targetStatus(),
                    queryResult.userReceivedAccount(),
                    queryResult.amountRefund(),
                    queryResult.rawPayload());
            log.info(
                    "退款申请异常后查单已同步：outRefundNo={}, status={}",
                    outRefundNo,
                    queryResult.targetStatus());
        } catch (RuntimeException queryEx) {
            log.warn(
                    "退款申请异常且查单失败，保留 PROCESSING 待补偿：outRefundNo={}",
                    outRefundNo,
                    queryEx);
        }
    }

    /**
     * 委托短事务落库（状态/累加/事件同事务，不含渠道 HTTP）。
     */
    private RefundOrderSnapshot applyChannelRefundResult(
            String outRefundNo,
            String outTradeNo,
            String channelRefundId,
            String channelTransactionId,
            String targetStatus,
            String userReceivedAccount,
            Integer channelAmountRefund,
            String rawPayload) {
        return refundChannelResultApplier.apply(
                outRefundNo,
                outTradeNo,
                channelRefundId,
                channelTransactionId,
                targetStatus,
                userReceivedAccount,
                channelAmountRefund,
                rawPayload);
    }

    /**
     * 仅微信明确业务拒绝码对应 {@link ResultCode#WECHAT_PAY_REFUND_FAILED} 时可关单。
     */
    private static boolean isClearRefundReject(BusinessException ex) {
        return ex != null && ex.getCode() == ResultCode.WECHAT_PAY_REFUND_FAILED.getCode();
    }

    /**
     * 异常退款入账账户：商户回收时写入可识别前缀，供业务侧区分「退给用户」与「退回商户」。
     */
    private static String resolveAbnormalReceiveAccount(String receiveType, String channelAccount) {
        if (AbnormalRefundReceiveType.MERCHANT_BANK_CARD.equalsIgnoreCase(receiveType)) {
            if (StringUtils.hasText(channelAccount)
                    && channelAccount.trim().toUpperCase().startsWith(AbnormalRefundReceiveType.MERCHANT_BANK_CARD)) {
                return channelAccount.trim();
            }
            return StringUtils.hasText(channelAccount)
                    ? AbnormalRefundReceiveType.MERCHANT_BANK_CARD + ":" + channelAccount.trim()
                    : AbnormalRefundReceiveType.MERCHANT_BANK_CARD;
        }
        return channelAccount;
    }

    private static RefundOrderSnapshot toSnapshot(PaymentRefundOrder order) {
        return new RefundOrderSnapshot(
                order.getOutRefundNo(),
                order.getOutTradeNo(),
                order.getBizOrderNo(),
                order.getProviderId(),
                order.getAmountRefund(),
                order.getAmountTotal(),
                order.getStatus(),
                order.getChannelRefundId(),
                order.getChannelTransactionId(),
                order.getReason(),
                order.getUserReceivedAccount(),
                order.getCreateTime(),
                order.getUpdateTime());
    }

    private static String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first.trim();
        }
        return StringUtils.hasText(second) ? second.trim() : null;
    }

    /**
     * 异常退款渠道调用准备结果。
     *
     * @param outRefundNo    平台退款单号
     * @param provider       退款 Provider
     * @param channelContext 渠道上下文
     */
    private record PreparedAbnormalRefund(
            String outRefundNo, RefundProvider provider, AbnormalRefundContext channelContext) {
    }
}
