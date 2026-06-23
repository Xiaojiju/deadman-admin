package com.mtfm.deadman.plugin.pay.spi;

import java.util.Collections;
import java.util.Map;

/**
 * 支付渠道回调上下文，由 Controller 层组装后传入 PayService。
 *
 * @param rawBody 回调请求体原文
 * @param headers 回调请求头
 */
public record PaymentNotifyContext(String rawBody, Map<String, String> headers) {

    /**
     * 构造无请求头的回调上下文。
     *
     * @param rawBody 回调请求体原文
     */
    public PaymentNotifyContext(String rawBody) {
        this(rawBody, Collections.emptyMap());
    }
}
