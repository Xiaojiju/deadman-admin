package com.mtfm.deadman.plugin.wechat.support;

import com.mtfm.deadman.common.exception.BusinessException;

/**
 * 按业务码构造异常，文案走本模块资源包中的 result.* 键。
 */
public final class WechatMessages {

    private WechatMessages() {}

    /**
     * 使用模块文案构造业务异常。
     *
     * @param code 业务码
     * @return 业务异常
     */
    public static BusinessException of(int code) {
        return switch (code) {
            case WechatErrorCodes.WECHAT_BIND_TOKEN_INVALID -> ex(code, "result.WECHAT_BIND_TOKEN_INVALID", "微信绑定临时令牌无效或已过期");
            default -> new BusinessException(code, "未知错误");
        };
    }

    private static BusinessException ex(int code, String key, String fallback) {
        return new BusinessException(code, key, fallback);
    }
}
