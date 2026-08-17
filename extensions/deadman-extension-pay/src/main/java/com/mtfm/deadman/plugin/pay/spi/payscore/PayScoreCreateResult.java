package com.mtfm.deadman.plugin.pay.spi.payscore;

import lombok.Builder;
import lombok.Getter;

/**
 * 创建支付分服务订单结果。
 */
@Getter
@Builder
public class PayScoreCreateResult {

    /** 商户服务订单号 */
    private final String outOrderNo;

    /** 渠道服务订单号 */
    private final String channelOrderId;

    /** 订单状态 */
    private final String state;

    /** 调起确认用的 package 信息（可选） */
    private final String packageInfo;

    /** 渠道原始响应（可选） */
    private final String rawResponse;
}
