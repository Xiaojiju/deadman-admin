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

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
