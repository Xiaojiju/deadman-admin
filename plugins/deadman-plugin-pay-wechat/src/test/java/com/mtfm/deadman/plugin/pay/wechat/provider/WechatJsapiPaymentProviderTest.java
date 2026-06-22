package com.mtfm.deadman.plugin.pay.wechat.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mtfm.deadman.plugin.pay.spi.PaymentNotifyContext;
import com.mtfm.deadman.plugin.pay.spi.PaymentNotifyResult;
import com.mtfm.deadman.plugin.pay.spi.PaymentPrepayContext;
import com.mtfm.deadman.plugin.pay.spi.PaymentPrepayResult;
import com.mtfm.deadman.plugin.pay.spi.PaymentQueryResult;
import com.mtfm.deadman.plugin.pay.wechat.client.WechatPayApiGateway;
import com.mtfm.deadman.plugin.pay.wechat.client.WechatPayJsapiPrepayResult;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayPluginProperties;
import com.mtfm.deadman.plugin.pay.wechat.config.WechatPayProviderBindingProperties;
import com.mtfm.deadman.plugin.pay.wechat.constant.WechatPayChannelParams;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatJsapiPrepayCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayNotifyParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayRequestPaymentParams;

/**
 * 微信 JSAPI PaymentProvider 单元测试（Mock 网关）。
 */
@ExtendWith(MockitoExtension.class)
class WechatJsapiPaymentProviderTest {

    @Mock
    private WechatPayApiGateway wechatPayApiGateway;

    @Mock
    private WechatPayPluginProperties wechatPayPluginProperties;

    private WechatJsapiPaymentProvider provider;

    @BeforeEach
    void setUp() {
        WechatPayProviderBindingProperties binding = new WechatPayProviderBindingProperties();
        binding.setEnabled(true);
        binding.setAppId("test_app_id");
        binding.setNotifyUrl("https://example.com/client/api/pay/wechat/jsapi/notify");
        lenient().when(wechatPayPluginProperties.providerBinding(WechatJsapiPaymentProvider.PROVIDER_ID)).thenReturn(binding);
        provider = new WechatJsapiPaymentProvider(wechatPayApiGateway, wechatPayPluginProperties);
    }

    @Test
    void shouldCreatePrepayViaWechatGateway() {
        when(wechatPayApiGateway.createJsapiPrepay(any(WechatJsapiPrepayCommand.class)))
                .thenReturn(new WechatPayJsapiPrepayResult(
                        "wx_prepay_123",
                        new WechatPayRequestPaymentParams("1710000000", "nonce", "prepay_id=wx_prepay_123", "RSA", "sign")));

        PaymentPrepayContext context = PaymentPrepayContext.builder()
                .bizOrderNo("BIZ20260618001")
                .description("会员月卡")
                .amountTotal(1)
                .payerUserId(100L)
                .channelParams(Map.of(WechatPayChannelParams.OPENID, "test_openid"))
                .build();
        PaymentPrepayResult result = provider.createPrepay(context, "PO20260622120000123456");

        assertThat(result.outTradeNo()).isEqualTo("PO20260622120000123456");
        assertThat(result.providerId()).isEqualTo("wechat-jsapi");
        assertThat(result.prepayId()).isEqualTo("wx_prepay_123");
        assertThat(result.clientInvokeParams().packageValue()).isEqualTo("prepay_id=wx_prepay_123");
        assertThat(result.channelExtra().openid()).isEqualTo("test_openid");

        verify(wechatPayApiGateway)
                .createJsapiPrepay(new WechatJsapiPrepayCommand(
                        "PO20260622120000123456",
                        "会员月卡",
                        1,
                        "test_openid",
                        "test_app_id",
                        "https://example.com/client/api/pay/wechat/jsapi/notify"));
    }

    @Test
    void shouldParseNotifyViaWechatGateway() {
        when(wechatPayApiGateway.parseNotify(any(PaymentNotifyContext.class)))
                .thenReturn(new WechatPayNotifyParseResult("PO20260622120000123456", "wx_tx_001", "SUCCESS"));

        PaymentNotifyResult result = provider.parseNotify(new PaymentNotifyContext(
                "{\"out_trade_no\":\"PO20260622120000123456\",\"trade_state\":\"SUCCESS\"}"));

        assertThat(result.outTradeNo()).isEqualTo("PO20260622120000123456");
        assertThat(result.channelTransactionId()).isEqualTo("wx_tx_001");
        assertThat(result.targetStatus()).isEqualTo("SUCCESS");
    }

    @Test
    void shouldQueryOrderViaWechatGateway() {
        when(wechatPayApiGateway.queryOrderByOutTradeNo("PO20260622120000123456"))
                .thenReturn(new WechatPayNotifyParseResult("PO20260622120000123456", "wx_tx_001", "SUCCESS"));

        PaymentQueryResult result = provider.queryOrder("PO20260622120000123456");

        assertThat(result.outTradeNo()).isEqualTo("PO20260622120000123456");
        assertThat(result.channelTransactionId()).isEqualTo("wx_tx_001");
        assertThat(result.targetStatus()).isEqualTo("SUCCESS");
        verify(wechatPayApiGateway).queryOrderByOutTradeNo("PO20260622120000123456");
    }
}
