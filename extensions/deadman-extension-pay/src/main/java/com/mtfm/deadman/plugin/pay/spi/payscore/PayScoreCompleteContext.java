package com.mtfm.deadman.plugin.pay.spi.payscore;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * 完结支付分服务订单上下文。
 */
@Getter
@Builder
public class PayScoreCompleteContext {

    /** 支付分服务 ID */
    private final String serviceId;

    /** 后付费项目列表 */
    private final List<PayScorePostPayment> postPayments;

    /** 总金额（分） */
    private final long totalAmountCents;

    /** 优惠项目列表（可选，结构与后付费项一致） */
    private final List<PayScorePostPayment> postDiscounts;

    /** 服务结束时间（可选） */
    private final String timeRangeEndTime;

    /** 完结时间（可选） */
    private final String completeTime;
}
