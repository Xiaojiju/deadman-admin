package com.mtfm.deadman.plugin.im.tencent.client.tls;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.Inflater;

import org.junit.jupiter.api.Test;

/**
 * {@link TencentImTlsSigApiV2} 单元测试，验证与官方 TLSSigAPIv2 算法一致的可解码结构。
 */
class TencentImTlsSigApiV2Test {

    /** 腾讯云文档示例测试密钥（非生产密钥） */
    private static final long SDK_APP_ID = 1400000000L;

    private static final String SECRET_KEY = "0123456789abcdef0123456789abcdef";

    @Test
    void shouldGenerateNonEmptyUserSigWithOfficialCharset() {
        TencentImTlsSigApiV2 sigApi = new TencentImTlsSigApiV2(SDK_APP_ID, SECRET_KEY);
        String userSig = sigApi.genUserSig("test_user", 86400L);

        assertThat(userSig).isNotBlank();
        assertThat(userSig).matches("[A-Za-z0-9\\*_\\-]+");

        String json = inflateSigDoc(userSig);
        assertThat(json).contains("\"TLS.ver\":\"2.0\"");
        assertThat(json).contains("\"TLS.identifier\":\"test_user\"");
        assertThat(json).contains("\"TLS.sdkappid\":" + SDK_APP_ID);
        assertThat(json).contains("\"TLS.sig\":");
    }

    private static String inflateSigDoc(String userSig) {
        byte[] encoded = userSig.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < encoded.length; ++i) {
            switch (encoded[i]) {
                case '*' -> encoded[i] = '+';
                case '-' -> encoded[i] = '/';
                case '_' -> encoded[i] = '=';
                default -> {
                    // 保持原字符
                }
            }
        }
        byte[] compressed = Base64.getDecoder().decode(encoded);
        Inflater inflater = new Inflater();
        inflater.setInput(compressed);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressed.length * 4);
        byte[] buffer = new byte[1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("UserSig 解压失败", ex);
        } finally {
            inflater.end();
        }
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}
