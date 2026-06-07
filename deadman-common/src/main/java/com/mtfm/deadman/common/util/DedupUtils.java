package com.mtfm.deadman.common.util;

import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 集合去重工具，保持首次出现顺序。
 */
public final class DedupUtils {

    private DedupUtils() {
    }

    /**
     * 对字符串集合去重，忽略 null 与空白项。
     * 
     * @param source 字符串集合
     * @return 去重后的字符串集合
     */
    public static Set<String> dedupeStrings(Collection<String> source) {
        if (source == null || source.isEmpty()) {
            return Set.of();
        }
        return source.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 对 Long 列表去重，保持顺序。
     * 
     * @param source 长整型列表
     * @return 去重后的长整型列表
     */
    public static List<Long> dedupeLongs(List<Long> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        return source.stream().distinct().toList();
    }
}
