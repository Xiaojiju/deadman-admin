package com.mtfm.deadman.plugin.im.tencent.client.tls;

import java.util.Base64;

/**
 * 腾讯云 TLS UserSig 专用 Base64URL 编码，与官方 TLSSigAPIv2 保持一致。
 */
final class TencentImTlsSigBase64Url {

    private TencentImTlsSigBase64Url() {
    }

    /**
     * 对字节数组做标准 Base64 编码后再替换为腾讯云 URL 安全字符集。
     *
     * @param input 原始字节
     * @return 编码后的字节（可直接转为字符串）
     */
    static byte[] encode(byte[] input) {
        byte[] base64 = Base64.getEncoder().encode(input);
        for (int i = 0; i < base64.length; ++i) {
            switch (base64[i]) {
                case '+' -> base64[i] = '*';
                case '/' -> base64[i] = '-';
                case '=' -> base64[i] = '_';
                default -> {
                    // 保持原字符
                }
            }
        }
        return base64;
    }
}
