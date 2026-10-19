package com.mtfm.deadman.support.client.file.web;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.file.support.FileErrorCodes;

/**
 * 标了 {@link BinaryErrorResponse} 的接口出错时，把 HTTP 状态码改成非 2xx。只改状态码，响应体和业务码不变；
 * 全局异常处理照旧生成 Result。
 * <p>映射：
 * <ul>
 *   <li>400xx（参数错）→ 400</li>
 *   <li>401xx（未登录、登录态不对）→ 401</li>
 *   <li>403xx（无权限）、404xx、13001（文件不存在）、注解里的 notFoundCodes → 404。无权限和不存在用同一个状态码，
 *       看不出文件是否存在</li>
 *   <li>5xxxx、13003（存储读取失败）、13004（存储不可用）→ 500</li>
 *   <li>其他业务码：原状态已经是 4xx/5xx 时保持，否则 400</li>
 * </ul>
 */
@RestControllerAdvice
public class BinaryErrorStatusAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response) {
        if (!(body instanceof Result<?> result) || result.isSuccess()
                || !(request instanceof ServletServerHttpRequest servletRequest)) {
            return body;
        }
        Object handler = servletRequest.getServletRequest()
                .getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return body;
        }
        BinaryErrorResponse binary = handlerMethod.getMethodAnnotation(BinaryErrorResponse.class);
        if (binary == null) {
            return body;
        }
        int current = response instanceof ServletServerHttpResponse servletResponse
                ? servletResponse.getServletResponse().getStatus() : HttpStatus.OK.value();
        response.setStatusCode(HttpStatus.valueOf(statusFor(result.getCode(), current, binary.notFoundCodes())));
        return body;
    }

    /**
     * 业务码到 HTTP 状态码。
     *
     * @param code 业务码
     * @param current 当前状态码
     * @param notFoundCodes 额外按不存在处理的业务码
     * @return HTTP 状态码
     */
    static int statusFor(int code, int current, int[] notFoundCodes) {
        for (int notFound : notFoundCodes) {
            if (code == notFound) {
                return HttpStatus.NOT_FOUND.value();
            }
        }
        if (code == FileErrorCodes.FILE_NOT_FOUND) {
            return HttpStatus.NOT_FOUND.value();
        }
        if (code == FileErrorCodes.FILE_STORAGE_ERROR || code == FileErrorCodes.FILE_PROVIDER_NOT_FOUND) {
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
        int family = code / 100;
        if (family == ResultCode.BAD_REQUEST.getCode() / 100) {
            return HttpStatus.BAD_REQUEST.value();
        }
        if (family == ResultCode.UNAUTHORIZED.getCode() / 100) {
            return HttpStatus.UNAUTHORIZED.value();
        }
        if (family == ResultCode.FORBIDDEN.getCode() / 100 || family == ResultCode.NOT_FOUND.getCode() / 100) {
            return HttpStatus.NOT_FOUND.value();
        }
        if (code >= 50000 && code < 60000) {
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
        return current >= 400 ? current : HttpStatus.BAD_REQUEST.value();
    }
}
