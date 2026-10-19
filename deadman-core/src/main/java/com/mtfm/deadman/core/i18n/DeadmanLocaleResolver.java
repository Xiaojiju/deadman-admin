package com.mtfm.deadman.core.i18n;

import com.mtfm.deadman.common.i18n.DeadmanLocales;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

/**
 * 按 {@code Accept-Language} 选择语言。接口无状态，不支持服务端写入语言。
 */
public class DeadmanLocaleResolver implements LocaleResolver {

    /**
     * 解析当前请求语言。
     *
     * @param request 当前请求
     * @return 简体中文、繁体中文、藏文或英文
     */
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return DeadmanLocales.resolve(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE));
    }

    /**
     * 无状态接口不接受服务端切换语言。
     *
     * @param request 当前请求
     * @param response 当前响应
     * @param locale 期望写入的语言
     */
    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        throw new UnsupportedOperationException("无状态接口通过 Accept-Language 选择语言");
    }
}
