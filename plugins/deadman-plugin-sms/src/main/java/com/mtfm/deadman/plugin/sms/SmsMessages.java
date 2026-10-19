package com.mtfm.deadman.plugin.sms;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;

/**
 * 短信插件文案，键位于 {@code i18n/deadman-plugin-sms/}。
 */
public final class SmsMessages {

    private SmsMessages() {}

    /**
     * 构造插件文案异常。
     *
     * @param code 业务码
     * @param key 文案键
     * @param fallback 缺省文案
     * @return 业务异常
     */
    public static BusinessException of(int code, String key, String fallback) {
        return new BusinessException(code, key, fallback);
    }

    /**
     * 手机号格式错误。
     *
     * @return 业务异常
     */
    public static BusinessException invalidPhone() {
        return new BusinessException(ResultCode.BAD_REQUEST.getCode(), "sms.phone.invalid", "手机号格式不正确");
    }
}
