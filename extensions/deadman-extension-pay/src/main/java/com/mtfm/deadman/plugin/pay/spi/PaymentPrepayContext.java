package com.mtfm.deadman.plugin.pay.spi;

import java.util.Collections;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

/**
 * 支付预下单上下文，由业务层在订单创建完成后组装并传入 Provider。
 */
@Getter
@Builder
public class PaymentPrepayContext {

    /** 业务订单号 */
    private final String bizOrderNo;

    /** 商品描述 */
    private final String description;

    /** 订单金额（分） */
    private final int amountTotal;

    /** 付款人用户 ID（业务侧用户主键） */
    private final Long payerUserId;

    /** 渠道扩展参数，如微信 JSAPI 的 openid */
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
