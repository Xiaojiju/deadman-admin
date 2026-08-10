package com.mtfm.deadman.plugin.pay.spi.refund;

/**
 * 平台退款单号生成 SPI，宿主应用可声明自定义 Bean 覆盖默认实现。
 */
public interface OutRefundNoSupplier {

    /**
     * 生成平台退款单号（out_refund_no）。
     *
     * @param context  退款上下文
     * @param provider 目标退款 Provider
     * @return 平台退款单号，需保证全局唯一且符合渠道长度限制
     */
    String generate(RefundContext context, RefundProvider provider);
}
