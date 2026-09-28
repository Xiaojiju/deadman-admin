package com.mtfm.deadman.plugin.im.tencent.support;

import com.mtfm.deadman.common.exception.BusinessException;

/**
 * 按业务码构造异常，文案走本模块资源包中的 result.* 键。
 */
public final class ImMessages {

    private ImMessages() {}

    /**
     * 使用模块文案构造业务异常。
     *
     * @param code 业务码
     * @return 业务异常
     */
    public static BusinessException of(int code) {
        return switch (code) {
            case ImErrorCodes.IM_CONFIG_INVALID -> ex(code, "result.IM_CONFIG_INVALID", "IM 插件配置无效");
            case ImErrorCodes.IM_REALM_UNKNOWN -> ex(code, "result.IM_REALM_UNKNOWN", "IM 用户域未注册");
            case ImErrorCodes.IM_USER_DISABLED -> ex(code, "result.IM_USER_DISABLED", "IM 用户已禁用");
            case ImErrorCodes.IM_ACCOUNT_SYNC_FAILED -> ex(code, "result.IM_ACCOUNT_SYNC_FAILED", "IM 账号同步失败");
            case ImErrorCodes.IM_USER_NOT_FOUND -> ex(code, "result.IM_USER_NOT_FOUND", "IM 用户映射不存在");
            default -> new BusinessException(code, "未知错误");
        };
    }

    private static BusinessException ex(int code, String key, String fallback) {
        return new BusinessException(code, key, fallback);
    }
}
