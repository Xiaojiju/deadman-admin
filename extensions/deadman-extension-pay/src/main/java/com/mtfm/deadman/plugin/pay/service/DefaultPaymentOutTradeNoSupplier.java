package com.mtfm.deadman.plugin.pay.service;

import com.mtfm.deadman.plugin.pay.spi.PaymentOutTradeNoSupplier;
import com.mtfm.deadman.plugin.pay.spi.PaymentPrepayContext;
import com.mtfm.deadman.plugin.pay.spi.PaymentProvider;
import com.mtfm.deadman.plugin.pay.util.PaymentOutTradeNoGenerator;

/**
 * 默认平台支付单号生成器，格式：PO + yyyyMMddHHmmss + 6 位随机数。
 */
public class DefaultPaymentOutTradeNoSupplier implements PaymentOutTradeNoSupplier {

    /**
     * {@inheritDoc}
     */
    @Override
    public String generate(PaymentPrepayContext context, PaymentProvider provider) {
        return PaymentOutTradeNoGenerator.generate();
    }
}
