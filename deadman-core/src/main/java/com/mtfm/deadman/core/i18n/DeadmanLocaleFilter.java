package com.mtfm.deadman.core.i18n;

import com.mtfm.deadman.common.i18n.DeadmanLocales;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 在安全过滤链之前写入当前请求语言，保证认证失败等早期响应也能按 {@code Accept-Language} 取文案。
 */
public class DeadmanLocaleFilter extends OncePerRequestFilter {

    /**
     * 绑定请求语言，请求结束后清理线程上的语言上下文。
     *
     * @param request 当前请求
     * @param response 当前响应
     * @param filterChain 后续过滤链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        LocaleContextHolder.setLocale(DeadmanLocales.resolve(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)));
        try {
            filterChain.doFilter(request, response);
        } finally {
            LocaleContextHolder.resetLocaleContext();
        }
    }
}
