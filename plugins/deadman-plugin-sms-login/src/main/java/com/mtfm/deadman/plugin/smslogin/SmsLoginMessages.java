package com.mtfm.deadman.plugin.smslogin;

import com.mtfm.deadman.common.i18n.MessageSourceHolder;

/**
 * 短信登录文案，键位于 {@code i18n/deadman-plugin-sms-login/}。
 */
public final class SmsLoginMessages {

    private SmsLoginMessages() {}

    /**
     * 按当前语言解析文案。
     *
     * @param key 文案键
     * @param fallback 缺省文案
     * @return 已解析文案
     */
    public static String text(String key, String fallback) {
        return MessageSourceHolder.resolve(key, fallback);
    }
}
