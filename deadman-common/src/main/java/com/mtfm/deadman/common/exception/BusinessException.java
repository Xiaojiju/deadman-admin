package com.mtfm.deadman.common.exception;

import com.mtfm.deadman.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常，携带可返回前端的业务码。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
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
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
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
    }
}
