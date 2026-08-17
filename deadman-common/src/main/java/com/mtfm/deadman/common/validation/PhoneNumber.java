package com.mtfm.deadman.common.validation;

/**
 * 中国大陆手机号校验与号码脱敏展示。
 */
public final class PhoneNumber {

    /** 大陆 11 位手机号正则（用于 {@link jakarta.validation.constraints.Pattern}） */
    public static final String PATTERN = "^1[3-9]\\d{9}$";
    /** 手机号格式校验失败提示 */
    public static final String MESSAGE = "手机号格式不正确";

    private PhoneNumber() {
    }

    /**
     * 脱敏展示电话号码：大陆手机号中间 4 位替换为 {@code *}，座机等其他号码中间 3 位替换为 {@code *}。
     *
     * @param phone 原始号码，可为空
     * @return 脱敏后的号码；空输入原样返回
     */
    public static String mask(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        String value = phone.trim();
        if (value.matches(PATTERN)) {
            // 138****5678
            return value.substring(0, 3) + "****" + value.substring(7);
        }
        if (value.length() <= 3) {
            return "*".repeat(value.length());
        }
        // 座机等：中间 3 位
        int maskLen = 3;
        int start = (value.length() - maskLen) / 2;
        return value.substring(0, start) + "***" + value.substring(start + maskLen);
    }
}
