package com.mtfm.deadman.plugin.pay.wechat.util;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mtfm.deadman.plugin.pay.spi.PaymentNotifyContext;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 微信支付回调 HTTP 工具。
 */
public final class WechatPayNotifyHttpUtils {

    private WechatPayNotifyHttpUtils() {
    }

    /**
     * 从 HTTP 请求提取微信回调头并组装上下文。
     *
     * @param body    回调请求体原文
     * @param request HTTP 请求
     * @return 回调上下文
     */
    public static PaymentNotifyContext toNotifyContext(String body, HttpServletRequest request) {
        return new PaymentNotifyContext(body, extractWechatHeaders(request));
    }

    private static Map<String, String> extractWechatHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name != null && name.regionMatches(true, 0, "Wechatpay-", 0, "Wechatpay-".length())) {
                headers.put(name, request.getHeader(name));
            }
        }
        return Map.copyOf(headers);
    }
}
