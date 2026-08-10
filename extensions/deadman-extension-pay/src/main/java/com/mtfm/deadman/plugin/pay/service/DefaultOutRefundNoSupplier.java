package com.mtfm.deadman.plugin.pay.service;

import com.mtfm.deadman.plugin.pay.spi.refund.OutRefundNoSupplier;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundContext;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundProvider;
import com.mtfm.deadman.plugin.pay.util.OutRefundNoGenerator;

/**
 * 默认平台退款单号生成器，格式：RF + yyyyMMddHHmmss + 6 位随机数。
 */
public class DefaultOutRefundNoSupplier implements OutRefundNoSupplier {

    /**
     * {@inheritDoc}
     */
    @Override
    public String generate(RefundContext context, RefundProvider provider) {
        return OutRefundNoGenerator.generate();
    }
}
