package com.mtfm.deadman.plugin.pay.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mtfm.deadman.plugin.pay.constant.PaymentOrderStatus;
import com.mtfm.deadman.plugin.pay.entity.PaymentOrder;
import com.mtfm.deadman.plugin.pay.manager.PaymentProviderManager;
import com.mtfm.deadman.plugin.pay.spi.PaymentOrderSnapshot;
import com.mtfm.deadman.plugin.pay.spi.PaymentOrderStatusChangedPublisher;
import com.mtfm.deadman.plugin.pay.spi.PaymentOutTradeNoSupplier;
import com.mtfm.deadman.plugin.pay.spi.PaymentProvider;
import com.mtfm.deadman.plugin.pay.spi.PaymentQueryResult;

/**
 * PayService 主动查单单元测试。
 */
@ExtendWith(MockitoExtension.class)
class PayServiceSyncOrderTest {

    @Mock
    private PaymentProviderManager paymentProviderManager;

    @Mock
    private PaymentOrderService paymentOrderService;

    @Mock
    private PaymentOutTradeNoSupplier paymentOutTradeNoSupplier;

    @Mock
    private PaymentOrderStatusChangedPublisher paymentOrderStatusChangedPublisher;

    @Mock
    private PaymentProvider paymentProvider;

    @InjectMocks
    private PayService payService;

    private PaymentOrder pendingOrder;

    @BeforeEach
    void setUp() {
        pendingOrder = PaymentOrder.builder()
                .outTradeNo("PO20260622120000123456")
                .bizOrderNo("BIZ001")
                .description("测试商品")
                .amountTotal(100)
                .status(PaymentOrderStatus.NOT_PAY)
                .payPlatform("WECHAT")
                .payMethod("JSAPI")
                .providerId("wechat-jsapi")
                .createTime(LocalDateTime.now().minusMinutes(5))
                .updateTime(LocalDateTime.now().minusMinutes(5))
                .build();
    }

    @Test
    void shouldApplyHandleNotifyLogicWhenChannelAlreadyPaid() {
        when(paymentOrderService.requireByOutTradeNo("PO20260622120000123456")).thenReturn(pendingOrder);
        when(paymentProviderManager.require("wechat-jsapi")).thenReturn(paymentProvider);
        when(paymentProvider.queryOrder("PO20260622120000123456"))
                .thenReturn(new PaymentQueryResult(
                        "PO20260622120000123456", "wx_tx_001", PaymentOrderStatus.SUCCESS, null));
        when(paymentOrderService.transitionStatus(
                        eq("PO20260622120000123456"), eq("wx_tx_001"), eq(PaymentOrderStatus.SUCCESS), eq(null)))
                .thenReturn(PaymentOrderStatus.NOT_PAY);

        PaymentOrder paidOrder = pendingOrder.toBuilder()
                .status(PaymentOrderStatus.SUCCESS)
                .channelTransactionId("wx_tx_001")
                .build();
        when(paymentOrderService.reload("PO20260622120000123456")).thenReturn(paidOrder);

        PaymentOrderSnapshot snapshot = payService.syncOrderFromChannel("PO20260622120000123456");

        assertThat(snapshot.status()).isEqualTo(PaymentOrderStatus.SUCCESS);
        verify(paymentProvider).queryOrder("PO20260622120000123456");
        verify(paymentOrderStatusChangedPublisher).publish(paidOrder, PaymentOrderStatus.NOT_PAY, PaymentOrderStatus.SUCCESS);
    }

    @Test
    void shouldSkipUpdateWhenChannelStatusUnchanged() {
        when(paymentOrderService.requireByOutTradeNo("PO20260622120000123456")).thenReturn(pendingOrder);
        when(paymentProviderManager.require("wechat-jsapi")).thenReturn(paymentProvider);
        when(paymentProvider.queryOrder("PO20260622120000123456"))
                .thenReturn(new PaymentQueryResult(
                        "PO20260622120000123456", null, PaymentOrderStatus.NOT_PAY, null));

        PaymentOrderSnapshot snapshot = payService.syncOrderFromChannel("PO20260622120000123456");

        assertThat(snapshot.status()).isEqualTo(PaymentOrderStatus.NOT_PAY);
        verify(paymentOrderService, never()).transitionStatus(any(), any(), any(), any());
        verify(paymentOrderStatusChangedPublisher, never()).publish(any(), any(), any());
    }

    @Test
    void shouldSkipChannelQueryWhenOrderAlreadyPaid() {
        PaymentOrder paidOrder = pendingOrder.toBuilder()
                .status(PaymentOrderStatus.SUCCESS)
                .channelTransactionId("wx_tx_existing")
                .build();
        when(paymentOrderService.requireByOutTradeNo("PO20260622120000123456")).thenReturn(paidOrder);

        PaymentOrderSnapshot snapshot = payService.syncOrderFromChannel("PO20260622120000123456");

        assertThat(snapshot.status()).isEqualTo(PaymentOrderStatus.SUCCESS);
        verify(paymentProviderManager, never()).require(any());
        verify(paymentProvider, never()).queryOrder(any());
    }
}
