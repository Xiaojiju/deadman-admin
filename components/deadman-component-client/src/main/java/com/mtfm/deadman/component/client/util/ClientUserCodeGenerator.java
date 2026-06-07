package com.mtfm.deadman.component.client.util;

import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * 生成用户端对外用户编码。
 */
public final class ClientUserCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();

    private ClientUserCodeGenerator() {
    }

    /**
     * 生成用户编码。
     *
     * @param prefix 前缀，如 CL
     * @return 用户编码
     */
    public static String generate(String prefix) {
        byte[] bytes = new byte[12];
        RANDOM.nextBytes(bytes);
        return prefix + HEX.formatHex(bytes).toUpperCase();
    }
}
