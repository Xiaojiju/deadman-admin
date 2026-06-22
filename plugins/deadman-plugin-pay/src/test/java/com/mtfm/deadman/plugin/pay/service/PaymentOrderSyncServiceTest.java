package com.mtfm.deadman.plugin.pay.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private PaymentOrderSyncService paymentOrderSyncService;

    private PayPluginProperties.Sync sync;

    @BeforeEach
    void setUp() {
        sync = new PayPluginProperties.Sync();
        sync.setMinAge(Duration.ofMinutes(2));
        sync.setMaxAge(Duration.ofMinutes(30));
        sync.setBatchSize(50);
        when(payPluginProperties.getSync()).thenReturn(sync);
    }

    @Test
    void shouldSyncEachPendingOrder() {
        PaymentOrder order = PaymentOrder.builder()
                .outTradeNo("PO202606221200PAID001")
                .status(PaymentOrderStatus.NOT_PAY)
                .build();
        when(paymentOrderService.listPendingForSync(Duration.ofMinutes(2), Duration.ofMinutes(30), 50))
                .thenReturn(List.of(order));
        when(payService.syncOrderFromChannel("PO202606221200PAID001"))
                .thenReturn(new PaymentOrderSnapshot(
                        "PO202606221200PAID001",
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

        paymentOrderSyncService.syncPendingOrders();

        verify(payService).syncOrderFromChannel("PO202606221200PAID001");
    }

    @Test
    void shouldDoNothingWhenNoPendingOrders() {
        when(paymentOrderService.listPendingForSync(any(), any(), eq(50))).thenReturn(List.of());

        paymentOrderSyncService.syncPendingOrders();

        verify(payService, never()).syncOrderFromChannel(any());
    }
}
