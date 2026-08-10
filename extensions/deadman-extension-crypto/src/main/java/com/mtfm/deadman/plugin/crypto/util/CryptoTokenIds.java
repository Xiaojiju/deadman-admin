package com.mtfm.deadman.plugin.crypto.util;

import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;

/**
 * 密文标识段（algorithm / keyId）字符集校验。
 *
 * <p>点分密文格式要求这两段不得包含 {@code '.'}，仅允许字母、数字、下划线与连字符。
 */
public final class CryptoTokenIds {

    private static final Pattern SAFE_ID = Pattern.compile("^[A-Za-z0-9_-]+$");

    private CryptoTokenIds() {
    }

    /**
     * 校验并返回 trim 后的标识；非法则抛业务异常。
     *
     * @param value 标识值
     * @param label 异常用标签（如 algorithm、keyId）
     * @return 合法标识
     */
    public static String requireSafe(String value, String label) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ResultCode.CRYPTO_PAYLOAD_INVALID, label + " 不能为空");
        }
        String trimmed = value.trim();
        if (!SAFE_ID.matcher(trimmed).matches()) {
            throw new BusinessException(
                    ResultCode.CRYPTO_CONFIG_INVALID,
                    label + " 仅允许字母数字下划线与连字符：" + trimmed);
        }
        return trimmed;
    }

    /**
     * 是否为合法标识（空视为非法）。
     *
     * @param value 标识
     * @return 合法时 true
     */
    public static boolean isSafe(String value) {
        return StringUtils.hasText(value) && SAFE_ID.matcher(value.trim()).matches();
    }
}
