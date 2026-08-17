package com.mtfm.deadman.plugin.im.tencent.util;

import org.springframework.util.StringUtils;

/**
 * 腾讯云 IM FaceUrl 校验工具：仅接受公网可直达的绝对 http(s) 地址。
 */
public final class ImFaceUrlSupport {

    private ImFaceUrlSupport() {
    }

    /**
     * 判断是否为可用于 FaceUrl 的绝对 http(s) URL。
     *
     * @param url 候选地址
     * @return 是否可用
     */
    public static boolean isAbsoluteHttpUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        String trimmed = url.trim();
        return trimmed.startsWith("https://") || trimmed.startsWith("http://");
    }

    /**
     * 规范化 FaceUrl：非绝对 http(s) 时返回 null（调用方应跳过写入 FaceUrl）。
     *
     * @param url 候选地址
     * @return 可用于同步的 URL，或 null
     */
    public static String normalizeOrNull(String url) {
        if (!isAbsoluteHttpUrl(url)) {
            return null;
        }
        return url.trim();
    }
}
