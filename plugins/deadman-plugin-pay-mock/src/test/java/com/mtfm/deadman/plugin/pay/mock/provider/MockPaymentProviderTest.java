package com.mtfm.deadman.plugin.pay.mock.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.plugin.pay.constant.PaymentMethod;
import com.mtfm.deadman.plugin.pay.constant.PaymentOrderStatus;
import com.mtfm.deadman.plugin.pay.constant.PaymentPlatform;
import com.mtfm.deadman.plugin.pay.mock.config.MockPayPluginProperties;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentNotifyResult;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentPrepayContext;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentPrepayResult;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentQueryResult;

/**
 * Mock PaymentProvider 单元测试。
 */
class MockPaymentProviderTest {

    private MockPaymentProvider provider;
    private MockPayPluginProperties properties;

    @BeforeEach
    void setUp() {
        properties = new MockPayPluginProperties();
        properties.setEnabled(true);
        properties.setAutoCompleteOnPrepay(true);
        provider = new MockPaymentProvider(properties);
    }

    @Test
    void shouldExposeMockIdentity() {
        assertThat(provider.providerId()).isEqualTo("mock");
        assertThat(provider.payPlatform()).isEqualTo(PaymentPlatform.MOCK);
        assertThat(provider.payMethod()).isEqualTo(PaymentMethod.MOCK);
        assertThat(provider.autoCompleteAfterPrepay()).isTrue();
    }

    @Test
    void shouldRespectAutoCompleteConfig() {
        properties.setAutoCompleteOnPrepay(false);
        assertThat(provider.autoCompleteAfterPrepay()).isFalse();
    }

    @Test
    void shouldCreatePrepayWithMockParams() {
        PaymentPrepayContext context = PaymentPrepayContext.builder()
                .bizOrderNo("BIZ20260723001")
                .description("Mock 订单")
                .amountTotal(100)
                .payerUserId(1L)
                .build();

        PaymentPrepayResult result = provider.createPrepay(context, "PO20260723120000123456");

        assertThat(result.outTradeNo()).isEqualTo("PO20260723120000123456");
        assertThat(result.providerId()).isEqualTo("mock");
        assertThat(result.prepayId()).startsWith("mock_prepay_");
        assertThat(result.clientInvokeParams()).isNotNull();
        assertThat(result.clientInvokeParams().packageValue()).startsWith("prepay_id=mock_prepay_");
        assertThat(result.clientInvokeParams().signType()).isEqualTo("MOCK");
        assertThat(result.clientInvokeParams().paySign()).isEqualTo("mock_pay_sign");
    }

    @Test
    void shouldQueryOrderAsSuccess() {
        PaymentQueryResult result = provider.queryOrder("PO20260723120000123456");

        assertThat(result.outTradeNo()).isEqualTo("PO20260723120000123456");
        assertThat(result.targetStatus()).isEqualTo(PaymentOrderStatus.SUCCESS);
        assertThat(result.channelTransactionId()).startsWith("mock_tx_");
    }

    @Test
    void shouldParseNotifySuccess() {
        PaymentNotifyResult result = provider.parseNotify(new ChannelNotifyContext(
                "{\"out_trade_no\":\"PO20260723120000123456\",\"transaction_id\":\"mock_tx_001\",\"status\":\"SUCCESS\"}"));

        assertThat(result.outTradeNo()).isEqualTo("PO20260723120000123456");
        assertThat(result.channelTransactionId()).isEqualTo("mock_tx_001");
        assertThat(result.targetStatus()).isEqualTo(PaymentOrderStatus.SUCCESS);
    }

    @Test
    void shouldGenerateTransactionIdWhenMissing() {
        PaymentNotifyResult result = provider.parseNotify(
                new ChannelNotifyContext("{\"out_trade_no\":\"PO20260723120000123456\",\"status\":\"SUCCESS\"}"));

        assertThat(result.channelTransactionId()).startsWith("mock_tx_");
    }

    @Test
    void shouldRejectNotifyWithoutRequiredFields() {
        assertThatThrownBy(() -> provider.parseNotify(new ChannelNotifyContext("{}")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("out_trade_no");
    }
}
