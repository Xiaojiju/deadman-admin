package com.mtfm.deadman.core.i18n;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 扫描 classpath 上 {@code i18n/<模块名>/messages*.properties}，得到资源包基名。
 * <p>
 * 语言后缀（如 {@code _zh_CN}、{@code _bo}）会去掉，多个语言文件对应同一个基名。
 */
public final class MessageBasenameScanner {

    /** 各模块约定目录 */
    static final String PATTERN = "classpath*:i18n/**/messages*.properties";

    private MessageBasenameScanner() {
    }

    /**
     * 扫描当前 classpath 中的模块文案目录。
     *
     * @return 去重后的基名，例如 {@code i18n/deadman-common/messages}
     */
    public static List<String> scan() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources;
        try {
            resources = resolver.getResources(PATTERN);
        } catch (IOException ex) {
            return List.of();
        }
        LinkedHashSet<String> basenames = new LinkedHashSet<>();
        for (Resource resource : resources) {
            String basename = toBasename(resource);
            if (basename != null) {
                basenames.add(basename);
            }
        }
        return List.copyOf(basenames);
    }

    /**
     * 从资源 URL 中截取 {@code i18n/.../messages}。
     *
     * @param resource 扫描到的 properties 文件
     * @return 基名；路径不符合约定时返回 {@code null}
     */
    static String toBasename(Resource resource) {
        String url;
        try {
            url = resource.getURL().toString();
        } catch (IOException ex) {
            return null;
        }
        int marker = url.indexOf("/i18n/");
        if (marker < 0) {
            return null;
        }
        String relative = url.substring(marker + 1);
        int slash = relative.lastIndexOf('/');
        if (slash < 0) {
            return null;
        }
        String filename = relative.substring(slash + 1);
        if (!filename.startsWith("messages") || !filename.endsWith(".properties")) {
            return null;
        }
        return relative.substring(0, slash) + "/messages";
    }
}
