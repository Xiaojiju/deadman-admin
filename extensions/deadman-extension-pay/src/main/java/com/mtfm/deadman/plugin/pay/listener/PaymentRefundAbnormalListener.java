package com.mtfm.deadman.plugin.pay.listener;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.plugin.pay.config.PayOrderSyncExecutorNames;
import com.mtfm.deadman.plugin.pay.constant.PaymentRefundStatus;
import com.mtfm.deadman.plugin.pay.entity.PaymentRefundOrder;
import com.mtfm.deadman.plugin.pay.event.PaymentRefundStatusChangedEvent;
import com.mtfm.deadman.plugin.pay.service.RefundService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 退款异常自动处理监听器：退款状态变为 ABNORMAL 且事务提交后，异步发起渠道异常退款。
 * <p>
 * 事件须在状态落库事务内发布（见 {@code RefundChannelResultApplier}），本监听仅使用
 * {@code AFTER_COMMIT}，避免无事务时误跑或在提交前外呼渠道。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.pay", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PaymentRefundAbnormalListener {

    private final RefundService refundService;

    /**
     * 事务提交后异步发起异常退款，避免回调线程与落库事务内嵌套渠道外呼。
     *
     * @param event 退款状态变更事件
     */
    @Async(PayOrderSyncExecutorNames.ASYNC_EXECUTOR_BEAN_NAME)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentRefundStatusChanged(PaymentRefundStatusChangedEvent event) {
        PaymentRefundOrder refundOrder = event.refundOrder();
        if (refundOrder == null || !StringUtils.hasText(refundOrder.getOutRefundNo())) {
            return;
        }
        if (!PaymentRefundStatus.ABNORMAL.equals(event.currentStatus())) {
            return;
        }
        if (PaymentRefundStatus.ABNORMAL.equals(event.previousStatus())) {
            return;
        }
        log.warn(
                "退款单进入 ABNORMAL（事务已提交），准备异步发起异常退款：outRefundNo={}, channelRefundId={}",
                refundOrder.getOutRefundNo(),
                refundOrder.getChannelRefundId());
        refundService.autoHandleAbnormalRefund(refundOrder.getOutRefundNo());
    }
}
