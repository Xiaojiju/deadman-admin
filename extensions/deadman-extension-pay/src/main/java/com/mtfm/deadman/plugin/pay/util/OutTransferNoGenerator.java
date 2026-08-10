package com.mtfm.deadman.plugin.pay.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 平台转账批次号 / 明细单号生成器。
 */
public final class OutTransferNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private OutTransferNoGenerator() {
    }

    /**
     * 生成批次号：TB + 时间 + 6 位随机数。
     *
     * @return 批次号
     */
    public static String generateBatchNo() {
        return "TB" + LocalDateTime.now().format(FORMATTER)
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }

    /**
     * 生成明细单号：TF + 时间 + 6 位随机数。
     *
     * @return 明细单号
     */
    public static String generateBillNo() {
        return "TF" + LocalDateTime.now().format(FORMATTER)
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }
}
