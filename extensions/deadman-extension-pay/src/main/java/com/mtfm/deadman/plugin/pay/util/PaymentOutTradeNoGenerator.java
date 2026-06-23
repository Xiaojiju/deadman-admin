package com.mtfm.deadman.plugin.pay.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 默认平台支付单号生成工具，供 {@link com.mtfm.deadman.plugin.pay.service.DefaultPaymentOutTradeNoSupplier} 使用。
 * 宿主自定义单号请实现 {@link com.mtfm.deadman.plugin.pay.spi.PaymentOutTradeNoSupplier}。
 */
public final class PaymentOutTradeNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private PaymentOutTradeNoGenerator() {
    }

    /**
     * 生成平台支付单号。
     *
     * @return out_trade_no
     */
    public static String generate() {
        int suffix = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "PO" + LocalDateTime.now().format(FORMATTER) + suffix;
    }
}
