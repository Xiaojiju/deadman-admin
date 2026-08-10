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

import com.mtfm.deadman.plugin.pay.config.PayPluginProperties;
import com.mtfm.deadman.plugin.pay.constant.PaymentOrderStatus;
import com.mtfm.deadman.plugin.pay.entity.PaymentOrder;
import com.mtfm.deadman.plugin.pay.manager.PaymentProviderManager;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentOrderSnapshot;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentOutTradeNoSupplier;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentProvider;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentQueryResult;

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
    private PaymentChannelResultApplier paymentChannelResultApplier;

    @Mock
    private PaymentOutTradeNoSupplier paymentOutTradeNoSupplier;

    @Mock
    private PayPluginProperties payPluginProperties;

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
                        "PO20260622120000123456", "wx_tx_001", PaymentOrderStatus.SUCCESS, null, null));
        PaymentOrderSnapshot paidSnapshot = new PaymentOrderSnapshot(
                "PO20260622120000123456",
                "BIZ001",
                "wechat-jsapi",
                "WECHAT",
                "JSAPI",
                "测试商品",
                100,
                0,
                PaymentOrderStatus.SUCCESS,
                null,
                "wx_tx_001",
                pendingOrder.getCreateTime(),
                pendingOrder.getUpdateTime());
        when(paymentChannelResultApplier.apply(
                        eq("PO20260622120000123456"),
                        eq("wx_tx_001"),
                        eq(PaymentOrderStatus.SUCCESS),
                        eq(null),
                        eq(null)))
                .thenReturn(paidSnapshot);

        PaymentOrderSnapshot snapshot = payService.syncOrderFromChannel("PO20260622120000123456");

        assertThat(snapshot.status()).isEqualTo(PaymentOrderStatus.SUCCESS);
        verify(paymentProvider).queryOrder("PO20260622120000123456");
        verify(paymentChannelResultApplier)
                .apply("PO20260622120000123456", "wx_tx_001", PaymentOrderStatus.SUCCESS, null, null);
    }

    @Test
    void shouldSkipUpdateWhenChannelStatusUnchanged() {
        when(paymentOrderService.requireByOutTradeNo("PO20260622120000123456")).thenReturn(pendingOrder);
        when(paymentProviderManager.require("wechat-jsapi")).thenReturn(paymentProvider);
        when(paymentProvider.queryOrder("PO20260622120000123456"))
                .thenReturn(new PaymentQueryResult(
                        "PO20260622120000123456", null, PaymentOrderStatus.NOT_PAY, null, null));

        PaymentOrderSnapshot snapshot = payService.syncOrderFromChannel("PO20260622120000123456");

        assertThat(snapshot.status()).isEqualTo(PaymentOrderStatus.NOT_PAY);
        verify(paymentChannelResultApplier, never()).apply(any(), any(), any(), any(), any());
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
