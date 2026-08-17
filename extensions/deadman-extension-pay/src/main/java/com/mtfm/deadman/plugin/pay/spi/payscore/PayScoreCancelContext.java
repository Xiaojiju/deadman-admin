package com.mtfm.deadman.plugin.pay.spi.payscore;

import lombok.Builder;
import lombok.Getter;

/**
 * 取消支付分服务订单上下文。
 */
@Getter
@Builder
public class PayScoreCancelContext {

    /** 支付分服务 ID */
    private final String serviceId;

    /** 取消原因 */
    private final String reason;

    /** 商户 AppId（可选，渠道可自行补齐） */
    private final String appId;
}
