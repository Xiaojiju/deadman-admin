package com.mtfm.deadman.plugin.crypto.util;

import java.nio.charset.StandardCharsets;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.plugin.crypto.spi.CryptoContext;

/**
 * GCM AAD（关联数据）构造：将模块与用途绑定到密文认证中，防止跨字段移植。
 *
 * <p>规则：{@code purpose} 为空时返回空数组（兼容审查前无 AAD 密文）；
 * 非空时为 UTF-8 编码的 {@code {module}|{purpose}}（module 空则前段为空字符串）。
 */
public final class CryptoAad {

    private CryptoAad() {
    }

    /**
     * 根据上下文构建 AAD。
     *
     * @param context 加解密上下文；{@code null} 视为无 AAD
     * @return AAD 字节，永不为 null
     */
    public static byte[] from(CryptoContext context) {
        if (context == null || !StringUtils.hasText(context.purpose())) {
            return new byte[0];
        }
        String module = context.moduleCode() == null ? "" : context.moduleCode().trim();
        String purpose = context.purpose().trim();
        return (module + "|" + purpose).getBytes(StandardCharsets.UTF_8);
    }
}
