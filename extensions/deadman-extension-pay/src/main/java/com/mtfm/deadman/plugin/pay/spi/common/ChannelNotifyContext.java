package com.mtfm.deadman.plugin.pay.spi.common;

import java.util.Collections;
import java.util.Map;

/**
 * 渠道回调上下文（支付 / 退款 / 后续转账等共用），由 Controller 组装后传入上层 Service。
 *
 * @param rawBody 回调请求体原文
 * @param headers 回调请求头
 */
public record ChannelNotifyContext(String rawBody, Map<String, String> headers) {

    /**
     * 构造无请求头的回调上下文。
     *
     * @param rawBody 回调请求体原文
     */
    public ChannelNotifyContext(String rawBody) {
        this(rawBody, Collections.emptyMap());
    }
}
