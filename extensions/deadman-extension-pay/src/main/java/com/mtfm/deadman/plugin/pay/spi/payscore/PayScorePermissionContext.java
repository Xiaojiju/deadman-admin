package com.mtfm.deadman.plugin.pay.spi.payscore;

import lombok.Builder;
import lombok.Getter;

/**
 * 支付分授权（预授权）上下文。
 */
@Getter
@Builder
public class PayScorePermissionContext {

    /** 支付分服务 ID */
    private final String serviceId;

    /** 商户授权协议号（可选，创建预授权时常用） */
    private final String authorizationCode;

    /** 用户 openid（可选） */
    private final String openid;

    /** 授权结果回调 URL（可选） */
    private final String notifyUrl;
}
