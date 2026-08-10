package com.mtfm.deadman.plugin.pay.spi.refund;

import java.util.Collections;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

/**
 * 退款申请上下文，由业务层或 RefundService 在校验支付单后组装并传入 Provider。
 */
@Getter
@Builder
public class RefundContext {

    /** 平台支付单号 */
    private final String outTradeNo;

    /** 渠道支付单号（可选，有则优先传给渠道） */
    private final String channelTransactionId;

    /** 业务订单号 */
    private final String bizOrderNo;

    /** 本次退款金额（分） */
    private final int amountRefund;

    /** 原支付订单总金额（分） */
    private final int amountTotal;

    /** 退款币种，默认 CNY */
    @Builder.Default
    private final String currency = "CNY";

    /** 退款原因（可选，部分渠道会展示给用户） */
    private final String reason;

    /** 渠道扩展参数 */
    @Builder.Default
    private final Map<String, String> channelParams = Collections.emptyMap();

    /**
     * 获取渠道扩展参数。
     *
     * @param key 参数键
     * @return 参数值，不存在时返回 null
     */
    public String channelParam(String key) {
        return channelParams == null ? null : channelParams.get(key);
    }
}
