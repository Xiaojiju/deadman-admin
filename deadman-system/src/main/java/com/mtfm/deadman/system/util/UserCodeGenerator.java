package com.mtfm.deadman.system.util;

import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * 生成对外暴露的用户基础编码。
 */
public final class UserCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();

    private UserCodeGenerator() {
    }

    /**
     * 生成用户基础编码
     * 
     * @param prefix 前缀
     * @return 用户基础编码
     */
    public static String generate(String prefix) {
        byte[] bytes = new byte[12];
        RANDOM.nextBytes(bytes);
        String body = HEX.formatHex(bytes).toUpperCase();
        return prefix + body;
    }
}
