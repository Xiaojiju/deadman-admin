package com.mtfm.deadman.common.exception;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.stream.Collectors;

/**
 * 全局异常处理，将异常映射为统一 {@link Result}。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：HTTP 200，body 内 code 为非 0 业务码 */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException ex) {
        return Result.of(ex.getCode(), ex.getMessage());
    }

    /** 参数校验失败（@Valid） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.of(ResultCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthentication(AuthenticationException ex) {
        return Result.of(ResultCode.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDenied(AccessDeniedException ex) {
        return Result.of(ResultCode.FORBIDDEN);
    }

    /**
     * 上传文件超过配置的大小上限。
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.warn("上传文件超过大小限制: {}", ex.getMessage());
        return Result.of(ResultCode.FILE_TOO_LARGE);
    }

    /**
     * 文件上传请求格式错误（非 multipart/form-data、缺少 file 字段或 Tomcat 包装的大小超限）。
     */
    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMultipart(MultipartException ex) {
        if (isUploadSizeExceeded(ex)) {
            log.warn("上传文件超过大小限制: {}", ex.getMessage());
            return Result.of(ResultCode.FILE_TOO_LARGE);
        }
        log.warn("文件上传请求格式错误: {}", ex.getMessage());
        return Result.of(
                ResultCode.BAD_REQUEST.getCode(),
                "请使用 multipart/form-data 上传，并包含名为 file 的文件字段");
    }

    /**
     * 缺少必填请求参数（如未传 file）。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingParameter(MissingServletRequestParameterException ex) {
        return Result.of(ResultCode.BAD_REQUEST.getCode(), "缺少必填参数：" + ex.getParameterName());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception ex) {
        if (isUploadSizeExceeded(ex)) {
            log.warn("上传文件超过大小限制: {}", ex.getMessage());
            return Result.of(ResultCode.FILE_TOO_LARGE);
        }
        log.error("未处理异常", ex);
        return Result.of(ResultCode.INTERNAL_ERROR);
    }

    /**
     * 判断异常链中是否包含文件大小超限（Spring 或 Tomcat 包装）。
     */
    private static boolean isUploadSizeExceeded(Throwable ex) {
        while (ex != null) {
            if (ex instanceof MaxUploadSizeExceededException) {
                return true;
            }
            String className = ex.getClass().getName();
            if (className.contains("FileSizeLimitExceededException")
                    || className.contains("SizeLimitExceededException")) {
                return true;
            }
            ex = ex.getCause();
        }
        return false;
    }
}
