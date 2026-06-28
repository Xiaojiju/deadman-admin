package com.mtfm.deadman.component.openauth.util;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.UUID;

/**
 * 开放授权凭证生成工具。
 */
public final class OpenAuthCredentialGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();

    private OpenAuthCredentialGenerator() {
    }

    /**
     * 生成 32 位 AppId（UUID 去横线）。
     *
     * @return AppId
     */
    public static String generateAppId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成 client_secret 明文（48 位十六进制）。
     *
     * @return client_secret
     */
    public static String generateClientSecret() {
        byte[] bytes = new byte[24];
        SECURE_RANDOM.nextBytes(bytes);
        return HEX.formatHex(bytes);
    }

    /**
     * 生成授权码（32 位十六进制）。
     *
     * @return auth_code
     */
    public static String generateAuthCode() {
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        return HEX.formatHex(bytes);
    }
}
