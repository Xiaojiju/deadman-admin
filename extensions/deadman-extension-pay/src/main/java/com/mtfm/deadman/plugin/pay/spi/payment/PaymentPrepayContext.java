package com.mtfm.deadman.plugin.pay.spi.payment;

import java.util.Collections;
import java.util.List;
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
     * 合单子单列表；非合单支付时为空。
     * <p>
     * 平台收付通合单支付时由业务层填充，每个子单对应一个二级商户收款。
     */
    @Builder.Default
    private final List<PaymentCombineSubOrder> subOrders = Collections.emptyList();

    /**
     * 获取渠道扩展参数。
     *
     * @param key 参数键
     * @return 参数值，不存在时返回 null
     */
    public String channelParam(String key) {
        return channelParams == null ? null : channelParams.get(key);
    }

    /**
     * 是否为合单支付上下文。
     *
     * @return 含子单时返回 true
     */
    public boolean isCombinePay() {
        return subOrders != null && !subOrders.isEmpty();
    }
}
