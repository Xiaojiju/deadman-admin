package com.mtfm.deadman.common.i18n;

import java.util.List;
import java.util.Locale;

/**
 * 项目支持的语言：简体中文、繁体中文、藏文、英文。
 * <p>
 * 未识别的 {@code Accept-Language} 回退到简体中文。繁体接受 {@code zh-TW}、{@code zh-HK}、{@code zh-MO}、{@code zh-Hant}。
 */
public final class DeadmanLocales {

    /** 简体中文 */
    public static final Locale SIMPLIFIED_CHINESE = Locale.SIMPLIFIED_CHINESE;

    /** 繁体中文 */
    public static final Locale TRADITIONAL_CHINESE = Locale.TRADITIONAL_CHINESE;

    /** 藏文 */
    public static final Locale TIBETAN = Locale.forLanguageTag("bo");

    /** 英文 */
    public static final Locale ENGLISH = Locale.ENGLISH;

    /** 缺省语言 */
    public static final Locale DEFAULT = SIMPLIFIED_CHINESE;

    /** 支持的语言，顺序固定 */
    public static final List<Locale> SUPPORTED = List.of(SIMPLIFIED_CHINESE, TRADITIONAL_CHINESE, TIBETAN, ENGLISH);

    private DeadmanLocales() {
    }

    /**
     * 按 {@code Accept-Language} 解析语言，按权重从高到低匹配。
     *
     * @param acceptLanguage 请求头原文，可为空
     * @return 匹配到的语言；无法识别时返回简体中文
     */
    public static Locale resolve(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return DEFAULT;
        }
        List<Locale.LanguageRange> ranges;
        try {
            ranges = Locale.LanguageRange.parse(acceptLanguage);
        } catch (IllegalArgumentException ex) {
            return DEFAULT;
        }
        for (Locale.LanguageRange range : ranges) {
            Locale matched = match(Locale.forLanguageTag(range.getRange()));
            if (matched != null) {
                return matched;
            }
        }
        return DEFAULT;
    }

    /**
     * 将单个语言标签归并到受支持的语言。
     *
     * @param locale 已解析的语言标签
     * @return 受支持的语言；不属于四种语言时返回 {@code null}
     */
    static Locale match(Locale locale) {
        if (locale == null) {
            return null;
        }
        String language = locale.getLanguage();
        if (language == null || language.isBlank()) {
            return null;
        }
        if ("en".equalsIgnoreCase(language)) {
            return ENGLISH;
        }
        if ("bo".equalsIgnoreCase(language)) {
            return TIBETAN;
        }
        if ("zh".equalsIgnoreCase(language)) {
            return matchChinese(locale);
        }
        return null;
    }

    /**
     * 中文按文字和地区区分简体、繁体。未标明时视为简体。
     */
    private static Locale matchChinese(Locale locale) {
        String script = locale.getScript();
        if ("Hant".equalsIgnoreCase(script)) {
            return TRADITIONAL_CHINESE;
        }
        if ("Hans".equalsIgnoreCase(script)) {
            return SIMPLIFIED_CHINESE;
        }
        String country = locale.getCountry();
        if ("TW".equalsIgnoreCase(country) || "HK".equalsIgnoreCase(country) || "MO".equalsIgnoreCase(country)) {
            return TRADITIONAL_CHINESE;
        }
        return SIMPLIFIED_CHINESE;
    }
}
