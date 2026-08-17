package com.mtfm.deadman.plugin.pay.spi.payscore;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

/**
 * 创建支付分服务订单上下文。
 */
@Getter
@Builder
public class PayScoreCreateContext {

    /** 支付分服务 ID（可空，由渠道 binding 回填） */
    private final String serviceId;

    /** 用户 openid */
    private final String openid;

    /** 服务信息介绍 */
    private final String serviceIntroduction;

    /** 风险金名称（如 DEPOSIT） */
    private final String riskFundName;

    /** 风险金金额（分） */
    private final long riskFundAmountCents;

    /** 服务开始时间（可选，如 OnAccept） */
    private final String timeRangeStartTime;

    /** 服务结束时间（可选） */
    private final String timeRangeEndTime;

    /** 服务位置名称（可选） */
    private final String locationName;

    /** 渠道回调 URL（可选，未传时用 binding） */
    private final String notifyUrl;

    /** 商户数据包（可选） */
    private final String attach;

    /** 是否需要用户确认，默认 true */
    @Builder.Default
    private final boolean needUserConfirm = true;

    /** 渠道扩展参数（可选） */
    private final Map<String, String> channelParams;
}
