package com.mtfm.deadman.plugin.pay.spi;

/**
 * 平台支付单号生成 SPI，宿主应用可声明自定义 Bean 覆盖默认实现。
 */
public interface PaymentOutTradeNoSupplier {

    /**
     * 生成平台支付单号（out_trade_no）。
     *
     * @param context  预下单上下文
     * @param provider 目标支付 Provider
     * @return 平台支付单号，需保证全局唯一且符合渠道长度限制
     */
    String generate(PaymentPrepayContext context, PaymentProvider provider);
}
