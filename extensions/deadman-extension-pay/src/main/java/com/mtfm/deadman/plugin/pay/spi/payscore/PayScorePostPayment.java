package com.mtfm.deadman.plugin.pay.spi.payscore;

import lombok.Builder;
import lombok.Getter;

/**
 * 支付分后付费项目（完结服务订单时的 {@code post_payments} 项）。
 */
@Getter
@Builder
public class PayScorePostPayment {

    /** 付费项目名称 */
    private final String name;

    /** 金额（分） */
    private final long amountCents;

    /** 付费说明（可选） */
    private final String description;

    /** 计费数量（可选） */
    private final Integer count;
}
