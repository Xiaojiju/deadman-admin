package com.mtfm.deadman.plugin.pay.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 默认平台退款单号生成工具，供 {@link com.mtfm.deadman.plugin.pay.service.DefaultOutRefundNoSupplier} 使用。
 * 宿主自定义单号请实现 {@link com.mtfm.deadman.plugin.pay.spi.refund.OutRefundNoSupplier}。
 */
public final class OutRefundNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private OutRefundNoGenerator() {
    }

    /**
     * 生成平台退款单号。
     *
     * @return out_refund_no
     */
    public static String generate() {
        int suffix = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "RF" + LocalDateTime.now().format(FORMATTER) + suffix;
    }
}
