package com.mtfm.deadman.plugin.ess.tencent.support;

import org.springframework.util.StringUtils;

/**
 * 电子签 UserData 约定：{@code bizType|bizRef}，便于多业务共用同一回调通道后分类处理。
 * <p>
 * 示例：{@code SIGN_ORDER|SGN1712345678900123}
 */
public final class EssUserDataSupport {

    /** UserData 分段分隔符 */
    public static final String SEPARATOR = "|";

    private EssUserDataSupport() {
    }

    /**
     * 编码业务透传数据。
     *
     * @param bizType 业务类型编码（如 SIGN_ORDER）
     * @param bizRef  业务单号或主键
     * @return UserData 字符串
     */
    public static String encode(String bizType, String bizRef) {
        if (!StringUtils.hasText(bizType)) {
            throw new IllegalArgumentException("bizType 不能为空");
        }
        if (!StringUtils.hasText(bizRef)) {
            throw new IllegalArgumentException("bizRef 不能为空");
        }
        return bizType.trim() + SEPARATOR + bizRef.trim();
    }

    /**
     * 解析业务类型；不符合约定时返回 null。
     *
     * @param userData 腾讯回调 / 发起时写入的 UserData
     * @return 业务类型
     */
    public static String parseBizType(String userData) {
        if (!StringUtils.hasText(userData)) {
            return null;
        }
        int idx = userData.indexOf(SEPARATOR);
        if (idx <= 0) {
            return null;
        }
        String bizType = userData.substring(0, idx).trim();
        return StringUtils.hasText(bizType) ? bizType : null;
    }

    /**
     * 解析业务引用；不符合约定时返回原文（兼容仅传业务单号的旧数据）。
     *
     * @param userData UserData
     * @return 业务引用
     */
    public static String parseBizRef(String userData) {
        if (!StringUtils.hasText(userData)) {
            return null;
        }
        int idx = userData.indexOf(SEPARATOR);
        if (idx <= 0 || idx >= userData.length() - 1) {
            return userData.trim();
        }
        String bizRef = userData.substring(idx + 1).trim();
        return StringUtils.hasText(bizRef) ? bizRef : null;
    }

    /**
     * 是否匹配指定业务类型。
     *
     * @param userData UserData
     * @param bizType  期望业务类型
     * @return 匹配则 true
     */
    public static boolean matchesBizType(String userData, String bizType) {
        if (!StringUtils.hasText(bizType)) {
            return false;
        }
        return bizType.trim().equalsIgnoreCase(parseBizType(userData));
    }
}
