package com.mtfm.deadman.support.client.file.web;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 标了 {@link BinaryErrorResponse} 的接口，请求头 Accept 只接受图片等非 JSON 类型（例如 {@code image/*}）时，
 * 错误 JSON 原本写不出来，会变成 500 空响应。这里把 Accept 换成 JSON 后交给原来的异常处理，状态码映射照旧由
 * {@link BinaryErrorStatusAdvice} 完成。Accept 本来就能接受 JSON 时不介入。
 */
@Component
public class BinaryErrorAcceptResolver implements HandlerExceptionResolver, Ordered {

    private final HandlerExceptionResolver delegate;

    /**
     * @param delegate Spring MVC 默认的异常处理组合（含全局 @ExceptionHandler）
     */
    public BinaryErrorAcceptResolver(@Lazy @Qualifier("handlerExceptionResolver") HandlerExceptionResolver delegate) {
        this.delegate = delegate;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        if (!(handler instanceof HandlerMethod handlerMethod)
                || !handlerMethod.hasMethodAnnotation(BinaryErrorResponse.class) || acceptsJson(request)) {
            return null;
        }
        return delegate.resolveException(new JsonAcceptRequest(request), response, handler, ex);
    }

    @Override
    public int getOrder() {
        // 排在 Spring Boot 的 DefaultErrorAttributes（HIGHEST_PRECEDENCE，只记录异常）之后
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    private static boolean acceptsJson(HttpServletRequest request) {
        Enumeration<String> values = request.getHeaders(HttpHeaders.ACCEPT);
        if (values == null || !values.hasMoreElements()) {
            return true;
        }
        while (values.hasMoreElements()) {
            try {
                for (MediaType type : MediaType.parseMediaTypes(values.nextElement())) {
                    if (type.isCompatibleWith(MediaType.APPLICATION_JSON)) {
                        return true;
                    }
                }
            } catch (RuntimeException ignored) {
                // Accept 写坏了：按不接受 JSON 处理
            }
        }
        return false;
    }

    /**
     * 只把 Accept 换成 application/json，其余照旧。
     */
    private static final class JsonAcceptRequest extends HttpServletRequestWrapper {

        JsonAcceptRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getHeader(String name) {
            return HttpHeaders.ACCEPT.equalsIgnoreCase(name) ? MediaType.APPLICATION_JSON_VALUE : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return HttpHeaders.ACCEPT.equalsIgnoreCase(name)
                    ? Collections.enumeration(List.of(MediaType.APPLICATION_JSON_VALUE))
                    : super.getHeaders(name);
        }
    }
}
