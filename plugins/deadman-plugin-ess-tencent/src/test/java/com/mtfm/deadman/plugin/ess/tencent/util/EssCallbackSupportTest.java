package com.mtfm.deadman.plugin.ess.tencent.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * 电子签回调解密与验签单测。
 */
class EssCallbackSupportTest {

    /**
     * 验签应生成 sha256= 前缀并匹配。
     */
    @Test
    void shouldVerifyCallbackSignature() {
        String payload = "{\"MsgType\":\"FlowStatusChange\"}";
        String token = "callback-token-demo";
        String signature = "sha256=" + EssCallbackVerifier.hmacSha256Hex(payload, token);
        assertTrue(EssCallbackVerifier.verify(payload, signature, token));
    }

    /**
     * AES 加解密应往返一致。
     */
    @Test
    void shouldEncryptAndDecryptCallbackPayload() throws Exception {
        String plain = "{\"FlowId\":\"yDxxxx\",\"FlowStatus\":\"ALL\"}";
        // AES-128 需要 16 字节密钥
        byte[] key = "1234567890abcdef".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = EssCallbackAes.aesEncrypt(plain.getBytes(StandardCharsets.UTF_8), key);
        byte[] decrypted = EssCallbackAes.aesDecrypt(encrypted, key);
        assertEquals(plain, new String(decrypted, StandardCharsets.UTF_8));
    }
}
