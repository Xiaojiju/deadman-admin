package com.mtfm.deadman.plugin.identity.support;

import java.time.DateTimeException;
import java.time.LocalDate;

/**
 * 中国大陆居民身份证号校验，并从号码中解析出生日期与性别。
 */
public final class IdCardSupport {

    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    private IdCardSupport() {}

    /**
     * 校验并规范化身份证号。
     *
     * @param raw 原始号码
     * @param birthDate 调用方填写的出生日期，非空时必须与号码一致
     * @param gender 调用方填写的性别，1 男 2 女；非空且非 0 时必须与号码一致
     * @return 解析结果
     */
    public static ParsedIdCard parse(String raw, LocalDate birthDate, Integer gender) {
        if (raw == null || raw.isBlank()) {
            throw IdentityMessages.badRequest("identity.id_card.empty", "身份证号不能为空");
        }
        String normalized = raw.trim().toUpperCase();
        if (!normalized.matches("^[1-9]\\d{16}[\\dX]$")) {
            throw IdentityMessages.badRequest("identity.id_card.format", "身份证号格式不正确");
        }
        if (checkCode(normalized) != normalized.charAt(17)) {
            throw IdentityMessages.badRequest("identity.id_card.check", "身份证号校验位不正确");
        }
        LocalDate parsedBirth = parseBirth(normalized);
        int parsedGender = ((normalized.charAt(16) - '0') % 2 == 1) ? 1 : 2;
        if (birthDate != null && !birthDate.equals(parsedBirth)) {
            throw IdentityMessages.badRequest("identity.id_card.birth_mismatch", "出生日期与身份证号不一致");
        }
        if (gender != null && gender != 0 && gender != parsedGender) {
            throw IdentityMessages.badRequest("identity.id_card.gender_mismatch", "性别与身份证号不一致");
        }
        return new ParsedIdCard(normalized, parsedBirth, parsedGender);
    }

    /**
     * 脱敏展示身份证号：保留前 6 位与后 4 位。
     *
     * @param idCardNo 身份证号
     * @return 脱敏后的号码；过短时原样返回
     */
    public static String mask(String idCardNo) {
        if (idCardNo == null || idCardNo.isBlank() || idCardNo.length() < 10) {
            return idCardNo;
        }
        String value = idCardNo.trim();
        return value.substring(0, 6) + "*".repeat(value.length() - 10) + value.substring(value.length() - 4);
    }

    private static LocalDate parseBirth(String normalized) {
        int year = Integer.parseInt(normalized.substring(6, 10));
        int month = Integer.parseInt(normalized.substring(10, 12));
        int day = Integer.parseInt(normalized.substring(12, 14));
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException ex) {
            throw IdentityMessages.badRequest("identity.id_card.birth_invalid", "身份证号中的出生日期无效");
        }
    }

    private static char checkCode(String normalized) {
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += (normalized.charAt(i) - '0') * WEIGHTS[i];
        }
        return CHECK_CODES[sum % 11];
    }

    /**
     * 身份证解析结果。
     *
     * @param idCardNo 规范化后的号码（末位 X 为大写）
     * @param birthDate 号码中的出生日期
     * @param gender 号码中的性别：1 男，2 女
     */
    public record ParsedIdCard(String idCardNo, LocalDate birthDate, int gender) {
    }
}
