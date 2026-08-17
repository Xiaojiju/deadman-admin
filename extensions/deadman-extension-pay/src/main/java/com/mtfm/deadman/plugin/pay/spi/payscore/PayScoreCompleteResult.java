package com.mtfm.deadman.plugin.pay.spi.payscore;

import lombok.Builder;
import lombok.Getter;

/**
 * 完结支付分服务订单结果。
 */
@Getter
@Builder
public class PayScoreCompleteResult {

    /** 商户服务订单号 */
    private final String outOrderNo;

    /** 渠道服务订单号 */
    private final String channelOrderId;

    /** 订单状态 */
    private final String state;
}
