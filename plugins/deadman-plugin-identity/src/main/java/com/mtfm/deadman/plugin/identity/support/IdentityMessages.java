package com.mtfm.deadman.plugin.identity.support;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;

/**
 * 实名插件文案。键位于 {@code i18n/deadman-plugin-identity/}。
 */
public final class IdentityMessages {

    private IdentityMessages() {}

    /**
     * 构造带插件文案键的业务异常。
     *
     * @param code 业务码
     * @param key 文案键
     * @param fallback 缺省文案
     * @param args 占位参数
     * @return 业务异常
     */
    public static BusinessException of(int code, String key, String fallback, Object... args) {
        return new BusinessException(code, key, fallback, args);
    }

    /**
     * 参数错误，业务码沿用通用 40000，文案仍在本插件。
     *
     * @param key 文案键
     * @param fallback 缺省文案
     * @return 业务异常
     */
    public static BusinessException badRequest(String key, String fallback) {
        return of(ResultCode.BAD_REQUEST.getCode(), key, fallback);
    }
}
