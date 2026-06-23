package com.mtfm.deadman.plugin.pay.wechat.util;

import java.util.Map;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.spi.PaymentNotifyContext;

/**
 * 微信支付回调请求头工具。
 */
public final class WechatPayNotifyHeaderUtils {

    private WechatPayNotifyHeaderUtils() {
    }

    /**
     * 从回调上下文中读取指定请求头（大小写不敏感）。
     *
     * @param context 回调上下文
     * @param name    请求头名称
     * @return 请求头值
     */
    public static String requireHeader(PaymentNotifyContext context, String name) {
        String value = findHeader(context.headers(), name);
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ResultCode.PAY_NOTIFY_PARSE_FAILED, "缺少微信支付回调请求头：" + name);
        }
        return value.trim();
    }

    private static String findHeader(Map<String, String> headers, String name) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        String direct = headers.get(name);
        if (StringUtils.hasText(direct)) {
            return direct;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
