package com.mtfm.deadman.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应封装，所有接口均返回此结构。
 * <p>成功时 code=0；业务失败时 code 为 {@link ResultCode} 或自定义业务码，HTTP 状态见全局异常处理。
 *
 * @param <T> 业务数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /** 业务响应码，0 表示成功 */
    private int code;
    /** 响应消息 */
    private String msg;
    /** 业务数据 */
    private T data;
    /** 响应时间戳（毫秒） */
    private Long timestamp;

    public static <T> Result<T> ok(T data) {
        return of(ResultCode.SUCCESS, data);
    }

    public static <T> Result<T> ok() {
        return of(ResultCode.SUCCESS, null);
    }

    public static <T> Result<T> of(ResultCode resultCode) {
        return of(resultCode, null);
    }

    public static <T> Result<T> of(ResultCode resultCode, T data) {
        return Result.<T>builder()
                .code(resultCode.getCode())
                .msg(resultCode.getMessage())
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> Result<T> of(int code, String msg) {
        return of(code, msg, null);
    }

    public static <T> Result<T> of(int code, String msg, T data) {
        return Result.<T>builder()
                .code(code)
                .msg(msg)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode() == this.code;
    }
}
