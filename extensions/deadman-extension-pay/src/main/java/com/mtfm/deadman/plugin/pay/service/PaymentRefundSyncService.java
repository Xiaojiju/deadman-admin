package com.mtfm.deadman.plugin.pay.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.plugin.pay.config.PayOrderSyncExecutorNames;
import com.mtfm.deadman.plugin.pay.config.PayPluginProperties;
import com.mtfm.deadman.plugin.pay.constant.PaymentRefundStatus;
import com.mtfm.deadman.plugin.pay.entity.PaymentRefundOrder;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundOrderSnapshot;

import lombok.extern.slf4j.Slf4j;

/**
 * 非终态退款单主动查退款与异常退款补偿服务。
 * 与定时触发解耦，宿主可关闭内置调度后自行调用本服务。
 */
@Slf4j
@Service
public class PaymentRefundSyncService {

    private final RefundService refundService;
    private final PaymentRefundOrderService paymentRefundOrderService;
    private final PayPluginProperties payPluginProperties;
    private final ApplicationContext applicationContext;
    private final ObjectProvider<Executor> payOrderSyncExecutorProvider;

    /**
     * @param refundService                退款门面
     * @param paymentRefundOrderService    退款单服务
     * @param payPluginProperties          插件配置
     * @param applicationContext           Spring 上下文
     * @param payOrderSyncExecutorProvider 并行线程池（复用支付查单池）
     */
    public PaymentRefundSyncService(
            RefundService refundService,
            PaymentRefundOrderService paymentRefundOrderService,
            PayPluginProperties payPluginProperties,
            ApplicationContext applicationContext,
            @Qualifier(PayOrderSyncExecutorNames.EXECUTOR_BEAN_NAME) ObjectProvider<Executor> payOrderSyncExecutorProvider) {
        this.refundService = refundService;
        this.paymentRefundOrderService = paymentRefundOrderService;
        this.payPluginProperties = payPluginProperties;
        this.applicationContext = applicationContext;
        this.payOrderSyncExecutorProvider = payOrderSyncExecutorProvider;
    }

    /**
     * 扫描并同步符合条件的非终态退款单；对未处理的 ABNORMAL 尝试异常退款补偿。
     */
    public void syncPendingRefunds() {
        PayPluginProperties.RefundSync sync = payPluginProperties.getRefundSync();
        List<PaymentRefundOrder> pending = paymentRefundOrderService.listPendingForSync(
                sync.getMinAge(), sync.getMaxAge(), sync.getBatchSize());
        if (pending.isEmpty()) {
            log.debug("无待主动查退款的退款单");
            return;
        }
        log.info("开始主动查退款，待处理数量={}，并行={}", pending.size(), sync.isParallelEnabled());
        if (sync.isParallelEnabled()) {
            syncInParallel(pending);
        } else {
            pending.forEach(this::syncOneSafely);
        }
    }

    private void syncInParallel(List<PaymentRefundOrder> pending) {
        Optional<Executor> executor = resolveExecutor();
        if (executor.isEmpty()) {
            log.warn("并行查退款已启用但未解析到可用线程池，回退为串行处理");
            pending.forEach(this::syncOneSafely);
            return;
        }
        CompletableFuture<?>[] futures = pending.stream()
                .map(order -> CompletableFuture.runAsync(() -> syncOneSafely(order), executor.get()))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).join();
    }

    private Optional<Executor> resolveExecutor() {
        String configuredBeanName = payPluginProperties.getSync().getExecutorBeanName();
        if (StringUtils.hasText(configuredBeanName)) {
            String beanName = configuredBeanName.trim();
            if (!applicationContext.containsBean(beanName)) {
                log.warn("配置的查单线程池 Bean 不存在：{}", beanName);
                return Optional.empty();
            }
            return Optional.of(applicationContext.getBean(beanName, Executor.class));
        }
        return Optional.ofNullable(payOrderSyncExecutorProvider.getIfAvailable());
    }

    private void syncOneSafely(PaymentRefundOrder order) {
        String outRefundNo = order.getOutRefundNo();
        try {
            if (PaymentRefundStatus.ABNORMAL.equals(order.getStatus())
                    && (order.getAbnormalHandled() == null || order.getAbnormalHandled() == 0)) {
                refundService.autoHandleAbnormalRefund(outRefundNo);
            }
            RefundOrderSnapshot snapshot = refundService.syncRefundFromChannel(outRefundNo);
            log.debug("主动查退款完成：outRefundNo={}, status={}", outRefundNo, snapshot.status());
        } catch (RuntimeException ex) {
            log.warn("主动查退款失败：outRefundNo={}", outRefundNo, ex);
        }
    }
}
