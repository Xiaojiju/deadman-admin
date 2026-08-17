package com.mtfm.deadman.plugin.pay.spi.payscore;

import lombok.Builder;
import lombok.Getter;

/**
 * 支付分回调解析结果。
 */
@Getter
@Builder
public class PayScoreNotifyResult {

    /** 商户服务订单号 */
    private final String outOrderNo;

    /** 渠道服务订单号 */
    private final String channelOrderId;

    /** 订单状态 */
    private final String state;

    /** 事件类型（可选） */
    private final String eventType;

    /** 总金额（分，可选） */
    private final Long totalAmountCents;

    /** 用户 openid（可选） */
    private final String openid;

    /** 回调原文 */
    private final String rawBody;
}
