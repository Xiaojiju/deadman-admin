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
import com.mtfm.deadman.plugin.pay.entity.PaymentOrder;
import com.mtfm.deadman.plugin.pay.spi.PaymentOrderSnapshot;

import lombok.extern.slf4j.Slf4j;

/**
 * 待支付单主动查单业务服务。
 * 与定时触发解耦，宿主可关闭内置调度后自行调用本服务接入外部任务框架。
 */
@Slf4j
@Service
public class PaymentOrderSyncService {

    private final PayService payService;
    private final PaymentOrderService paymentOrderService;
    private final PayPluginProperties payPluginProperties;
    private final ApplicationContext applicationContext;
    private final ObjectProvider<Executor> payOrderSyncExecutorProvider;

    /**
     * @param payService                   支付门面
     * @param paymentOrderService          订单服务
     * @param payPluginProperties          插件配置
     * @param applicationContext           Spring 上下文，用于按名称解析宿主线程池
     * @param payOrderSyncExecutorProvider 插件默认查单线程池（可选）
     */
    public PaymentOrderSyncService(
            PayService payService,
            PaymentOrderService paymentOrderService,
            PayPluginProperties payPluginProperties,
            ApplicationContext applicationContext,
            @Qualifier(PayOrderSyncExecutorNames.EXECUTOR_BEAN_NAME) ObjectProvider<Executor> payOrderSyncExecutorProvider) {
        this.payService = payService;
        this.paymentOrderService = paymentOrderService;
        this.payPluginProperties = payPluginProperties;
        this.applicationContext = applicationContext;
        this.payOrderSyncExecutorProvider = payOrderSyncExecutorProvider;
    }

    /**
     * 扫描并同步符合条件的待支付单。
     * 供内置定时任务或外部调度框架调用。
     */
    public void syncPendingOrders() {
        PayPluginProperties.Sync sync = payPluginProperties.getSync();
        List<PaymentOrder> pendingOrders = paymentOrderService.listPendingForSync(
                sync.getMinAge(), sync.getMaxAge(), sync.getBatchSize());
        if (pendingOrders.isEmpty()) {
            log.debug("无待主动查单的支付单");
            return;
        }
        log.info("开始主动查单，待处理数量={}，并行={}", pendingOrders.size(), sync.isParallelEnabled());
        if (sync.isParallelEnabled()) {
            syncPendingOrdersInParallel(pendingOrders);
        } else {
            pendingOrders.forEach(order -> syncOrderSafely(order.getOutTradeNo()));
        }
    }

    /**
     * 主动查单并同步单笔支付单状态。
     *
     * @param outTradeNo 平台支付单号
     * @return 同步后的支付单快照
     */
    public PaymentOrderSnapshot syncOrder(String outTradeNo) {
        return payService.syncOrderFromChannel(outTradeNo);
    }

    /**
     * 并行查单。
     *
     * @param pendingOrders 待支付单列表
     */
    private void syncPendingOrdersInParallel(List<PaymentOrder> pendingOrders) {
        Optional<Executor> executor = resolveExecutor();
        if (executor.isEmpty()) {
            log.warn("并行查单已启用但未解析到可用线程池，回退为串行处理");
            pendingOrders.forEach(order -> syncOrderSafely(order.getOutTradeNo()));
            return;
        }
        CompletableFuture<?>[] futures = pendingOrders.stream()
                .map(order -> CompletableFuture.runAsync(() -> syncOrderSafely(order.getOutTradeNo()), executor.get()))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).join();
    }

    /**
     * 解析并行查单线程池，优先级：配置 Bean 名 &gt; 编码注入 {@code payOrderSyncExecutor} &gt; 插件自建池。
     *
     * @return 可用线程池
     */
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

    /**
     * 串行查单（单笔容错）。
     *
     * @param outTradeNo 平台支付单号
     */
    private void syncOrderSafely(String outTradeNo) {
        try {
            PaymentOrderSnapshot snapshot = payService.syncOrderFromChannel(outTradeNo);
            log.debug("主动查单完成：outTradeNo={}, status={}", outTradeNo, snapshot.status());
        } catch (RuntimeException ex) {
            log.warn("主动查单失败：outTradeNo={}", outTradeNo, ex);
        }
    }
}
