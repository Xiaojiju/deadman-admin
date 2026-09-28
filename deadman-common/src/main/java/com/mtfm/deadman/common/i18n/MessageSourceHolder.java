package com.mtfm.deadman.common.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;

import java.util.Locale;

/**
 * 当前应用的 {@link MessageSource} 持有者，供统一响应和业务异常在请求线程内取文案。
 * <p>
 * 由 core 模块在组装资源包后绑定。尚未绑定时直接返回调用方给出的默认文案。
 */
public final class MessageSourceHolder {

    private static volatile MessageSource messageSource = new StaticMessageSource();

    private MessageSourceHolder() {
    }

    /**
     * 绑定应用级文案源。重复调用会替换上一次绑定。
     *
     * @param source 文案源；为空时恢复为空实现
     */
    public static void bind(MessageSource source) {
        messageSource = source == null ? new StaticMessageSource() : source;
    }

    /**
     * 按当前请求语言解析文案。
     *
     * @param code 文案键
     * @param defaultMessage 资源包缺失时的回退文案
     * @param args 文案占位参数，对应 {@code {0}}、{@code {1}}
     * @return 已解析文案
     */
    public static String resolve(String code, String defaultMessage, Object... args) {
        if (code == null || code.isBlank()) {
            return defaultMessage;
        }
        Locale locale = currentLocale();
        Object[] arguments = args == null || args.length == 0 ? null : args;
        return messageSource.getMessage(code, arguments, defaultMessage, locale);
    }

    /**
     * 取当前请求语言。请求尚未进入语言过滤器时使用简体中文，不跟随操作系统语言。
     */
    private static Locale currentLocale() {
        LocaleContext localeContext = LocaleContextHolder.getLocaleContext();
        if (localeContext == null || localeContext.getLocale() == null) {
            return DeadmanLocales.DEFAULT;
        }
        return localeContext.getLocale();
    }
}
