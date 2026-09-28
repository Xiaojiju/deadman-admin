package com.mtfm.deadman.common.exception;

import com.mtfm.deadman.common.i18n.MessageSourceHolder;
import com.mtfm.deadman.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常，携带可返回前端的业务码。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    /** 资源包文案键；显式传入对外文案时为空，响应阶段不再翻译 */
    private final String messageKey;

    /** 文案占位参数，对应资源包中的 {0}、{1} */
    private final Object[] messageArgs;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.messageKey = resultCode.messageKey();
        this.messageArgs = null;
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.messageKey = null;
        this.messageArgs = null;
    }

    /**
     * 带根因的业务异常（便于并行网关、流程错误处理器保留原始堆栈）。
     *
     * @param resultCode 业务码
     * @param message 对外说明
     * @param cause 原始异常
     */
    public BusinessException(ResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.code = resultCode.getCode();
        this.messageKey = null;
        this.messageArgs = null;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.messageKey = null;
        this.messageArgs = null;
    }

    /**
     * 使用模块自己的资源包文案。键放在该模块的 {@code i18n/<模块名>/messages*.properties}，不要写入 common。
     *
     * @param code 业务码
     * @param messageKey 文案键
     * @param defaultMessage 资源包缺失时的回退文案
     * @param messageArgs 占位参数
     */
    public BusinessException(int code, String messageKey, String defaultMessage, Object... messageArgs) {
        super(defaultMessage);
        this.code = code;
        this.messageKey = messageKey;
        this.messageArgs = messageArgs == null || messageArgs.length == 0 ? null : messageArgs.clone();
    }

    /**
     * 带根因的业务异常。
     *
     * @param code 业务码
     * @param message 对外说明
     * @param cause 原始异常
     */
    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.messageKey = null;
        this.messageArgs = null;
    }

    /**
     * 按当前请求语言解析对外文案。
     *
     * @return 已翻译文案；未绑定文案键时返回构造时传入的原文
     */
    public String resolveMessage() {
        if (messageKey == null || messageKey.isBlank()) {
            return getMessage();
        }
        return MessageSourceHolder.resolve(messageKey, getMessage(), messageArgs == null ? new Object[0] : messageArgs);
    }
}
