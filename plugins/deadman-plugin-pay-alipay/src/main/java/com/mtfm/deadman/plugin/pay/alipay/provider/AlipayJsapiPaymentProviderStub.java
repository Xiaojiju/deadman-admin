package com.mtfm.deadman.plugin.pay.alipay.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.alipay.constant.AlipayPayProviderIds;
import com.mtfm.deadman.plugin.pay.constant.PaymentMethod;
import com.mtfm.deadman.plugin.pay.constant.PaymentPlatform;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentNotifyResult;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentPrepayContext;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentPrepayResult;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentProvider;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentQueryResult;

/**
 * 支付宝直连支付 Provider 占位实现。
 * <p>
 * 启用插件后仍会拒绝真实下单，直至接入官方 SDK；业务侧请继续走 {@code DirectPayFacade}。
 */
@Component
@ConditionalOnProperty(
        prefix = "deadman.plugin.pay-alipay.providers.alipay-jsapi",
        name = "enabled",
        havingValue = "true")
public class AlipayJsapiPaymentProviderStub implements PaymentProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public String providerId() {
        return AlipayPayProviderIds.ALIPAY_JSAPI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String payPlatform() {
        return PaymentPlatform.ALIPAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String payMethod() {
        return PaymentMethod.JSAPI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentPrepayResult createPrepay(PaymentPrepayContext context, String outTradeNo) {
        throw notImplemented("createPrepay");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentNotifyResult parseNotify(ChannelNotifyContext context) {
        throw notImplemented("parseNotify");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentQueryResult queryOrder(String outTradeNo) {
        throw notImplemented("queryOrder");
    }

    private static BusinessException notImplemented(String action) {
        return new BusinessException(
                ResultCode.BAD_REQUEST,
                "支付宝 Provider 骨架尚未实现渠道调用：" + action + "，请接入支付宝 SDK 后替换本 Stub");
    }
}
