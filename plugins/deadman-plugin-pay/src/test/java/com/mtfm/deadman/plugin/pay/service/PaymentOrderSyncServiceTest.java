package com.mtfm.deadman.plugin.pay.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;

import com.mtfm.deadman.plugin.pay.config.PayPluginProperties;
import com.mtfm.deadman.plugin.pay.constant.PaymentOrderStatus;
import com.mtfm.deadman.plugin.pay.entity.PaymentOrder;
import com.mtfm.deadman.plugin.pay.spi.PaymentOrderSnapshot;

/**
 * PaymentOrderSyncService 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class PaymentOrderSyncServiceTest {

    @Mock
    private PayService payService;

    @Mock
    private PaymentOrderService paymentOrderService;

    @Mock
    private PayPluginProperties payPluginProperties;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ObjectProvider<Executor> payOrderSyncExecutorProvider;

    private PaymentOrderSyncService paymentOrderSyncService;

    private PayPluginProperties.Sync sync;

    @BeforeEach
    void setUp() {
        paymentOrderSyncService = new PaymentOrderSyncService(
                payService, paymentOrderService, payPluginProperties, applicationContext, payOrderSyncExecutorProvider);
        sync = new PayPluginProperties.Sync();
        sync.setMinAge(Duration.ofMinutes(2));
        sync.setMaxAge(Duration.ofMinutes(30));
        sync.setBatchSize(50);
        when(payPluginProperties.getSync()).thenReturn(sync);
    }

    @Test
    void shouldSyncEachPendingOrderSequentiallyWhenParallelDisabled() {
        sync.setParallelEnabled(false);
        PaymentOrder order = pendingOrder("PO202606221200PAID001");
        when(paymentOrderService.listPendingForSync(Duration.ofMinutes(2), Duration.ofMinutes(30), 50))
                .thenReturn(List.of(order));
        stubPaidSnapshot("PO202606221200PAID001");

        paymentOrderSyncService.syncPendingOrders();

        verify(payService).syncOrderFromChannel("PO202606221200PAID001");
        verify(applicationContext, never()).containsBean(any());
    }

    @Test
    void shouldUseConfiguredExecutorBeanNameWhenPresent() {
        sync.setParallelEnabled(true);
        sync.setExecutorBeanName("applicationTaskExecutor");
        PaymentOrder order = pendingOrder("PO202606221200PAID001");
        when(paymentOrderService.listPendingForSync(Duration.ofMinutes(2), Duration.ofMinutes(30), 50))
                .thenReturn(List.of(order));
        when(applicationContext.containsBean("applicationTaskExecutor")).thenReturn(true);
        when(applicationContext.getBean("applicationTaskExecutor", Executor.class)).thenReturn(Runnable::run);
        stubPaidSnapshot("PO202606221200PAID001");

        paymentOrderSyncService.syncPendingOrders();

        verify(applicationContext).getBean("applicationTaskExecutor", Executor.class);
        verify(payOrderSyncExecutorProvider, never()).getIfAvailable();
        verify(payService).syncOrderFromChannel("PO202606221200PAID001");
    }

    @Test
    void shouldUseInjectedPayOrderSyncExecutorWhenNoConfiguredBeanName() {
        sync.setParallelEnabled(true);
        PaymentOrder order = pendingOrder("PO202606221200PAID001");
        when(paymentOrderService.listPendingForSync(Duration.ofMinutes(2), Duration.ofMinutes(30), 50))
                .thenReturn(List.of(order));
        when(payOrderSyncExecutorProvider.getIfAvailable()).thenReturn(Runnable::run);
        stubPaidSnapshot("PO202606221200PAID001");

        paymentOrderSyncService.syncPendingOrders();

        verify(payOrderSyncExecutorProvider).getIfAvailable();
        verify(payService).syncOrderFromChannel("PO202606221200PAID001");
    }

    @Test
    void shouldDoNothingWhenNoPendingOrders() {
        when(paymentOrderService.listPendingForSync(any(), any(), eq(50))).thenReturn(List.of());

        paymentOrderSyncService.syncPendingOrders();

        verify(payService, never()).syncOrderFromChannel(any());
    }

    private static PaymentOrder pendingOrder(String outTradeNo) {
        return PaymentOrder.builder()
                .outTradeNo(outTradeNo)
                .status(PaymentOrderStatus.NOT_PAY)
                .build();
    }

    private void stubPaidSnapshot(String outTradeNo) {
        when(payService.syncOrderFromChannel(outTradeNo))
                .thenReturn(new PaymentOrderSnapshot(
                        outTradeNo,
                        "BIZ001",
                        "wechat-jsapi",
                        "WECHAT",
                        "JSAPI",
                        "测试",
                        100,
                        PaymentOrderStatus.SUCCESS,
                        "prepay",
                        "wx_tx",
                        null,
                        null));
    }
}
