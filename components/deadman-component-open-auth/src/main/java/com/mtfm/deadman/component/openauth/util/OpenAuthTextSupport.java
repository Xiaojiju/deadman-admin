package com.mtfm.deadman.component.openauth.util;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 开放授权 scope 与 realm 字符串解析工具。
 */
public final class OpenAuthTextSupport {

    private OpenAuthTextSupport() {
    }

    /**
     * 解析逗号分隔字符串为列表。
     *
     * @param raw 原始字符串
     * @return 去重后的非空列表
     */
    public static List<String> splitCommaValues(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    /**
     * 列表转为逗号分隔字符串。
     *
     * @param values 值列表
     * @return 逗号分隔字符串
     */
    public static String joinCommaValues(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return String.join(",", values);
    }
}
