package com.mtfm.deadman.plugin.pay.spi.payscore;

import lombok.Builder;
import lombok.Getter;

/**
 * 查询支付分服务订单结果。
 */
@Getter
@Builder
public class PayScoreQueryResult {

    /** 商户服务订单号 */
    private final String outOrderNo;

    /** 渠道服务订单号 */
    private final String channelOrderId;

    /** 订单状态 */
    private final String state;

    /** 状态描述 */
    private final String stateDescription;

    /** 订单总金额（分，可选） */
    private final Long totalAmountCents;

    /** 渠道原始响应（可选） */
    private final String raw;
}
