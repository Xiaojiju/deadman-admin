package com.mtfm.deadman.plugin.pay.spi.payscore;

import lombok.Builder;
import lombok.Getter;

/**
 * 支付分授权查询/创建结果。
 */
@Getter
@Builder
public class PayScorePermissionResult {

    /** 支付分服务 ID */
    private final String serviceId;

    /** 用户 openid（可选） */
    private final String openid;

    /** 授权协议号（可选） */
    private final String authorizationCode;

    /** 授权状态 */
    private final String authorizationState;

    /** 授权/订单侧标识（如 openid 关联单号，可选） */
    private final String openOrOrderId;
}
