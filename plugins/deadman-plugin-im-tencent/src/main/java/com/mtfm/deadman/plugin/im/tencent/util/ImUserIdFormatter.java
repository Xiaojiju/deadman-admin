package com.mtfm.deadman.plugin.im.tencent.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.plugin.im.tencent.config.ImTencentPluginProperties;

/**
 * 将业务主体格式化为腾讯云 IM UserID（最长 32 字节）。
 */
@Component
public class ImUserIdFormatter {

    private static final int MAX_USER_ID_BYTES = 32;

    private final ImTencentPluginProperties properties;

    /**
     * @param properties 插件配置
     */
    public ImUserIdFormatter(ImTencentPluginProperties properties) {
        this.properties = properties;
    }

    /**
     * 生成腾讯云 IM UserID。
     *
     * @param realmId   用户域标识
     * @param subjectId 域内主键
     * @return IM UserID
     */
    public String format(String realmId, String subjectId) {
        String template = properties.getUserIdTemplate();
        String candidate = template.replace("{realm}", sanitize(realmId)).replace("{subjectId}", sanitize(subjectId));
        candidate = truncateByUtf8Bytes(candidate, MAX_USER_ID_BYTES);
        if (StringUtils.hasText(candidate) && utf8Length(candidate) <= MAX_USER_ID_BYTES) {
            return candidate;
        }
        String hash = sha256Hex(realmId + ":" + subjectId).substring(0, 24);
        return truncateByUtf8Bytes(sanitize(realmId) + "_" + hash, MAX_USER_ID_BYTES);
    }

    private static String sanitize(String value) {
        if (!StringUtils.hasText(value)) {
            return "unknown";
        }
        StringBuilder builder = new StringBuilder(value.length());
        for (char ch : value.trim().toCharArray()) {
            if (Character.isLetterOrDigit(ch) || ch == '_') {
                builder.append(ch);
            } else {
                builder.append('_');
            }
        }
        return builder.toString();
    }

    private static int utf8Length(String value) {
        return value.getBytes(StandardCharsets.UTF_8).length;
    }

    private static String truncateByUtf8Bytes(String value, int maxBytes) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) {
            return value;
        }
        int length = value.length();
        while (length > 0 && value.substring(0, length).getBytes(StandardCharsets.UTF_8).length > maxBytes) {
            length--;
        }
        return value.substring(0, length);
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 不可用", ex);
        }
    }
}
