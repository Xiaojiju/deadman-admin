package com.mtfm.deadman.plugin.ess.tencent.util;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;

/**
 * 腾讯电子签回调签名校验工具。
 * <p>
 * 对齐官方 ess-java-kit {@code CallbackVerify}：{@code Content-Signature = sha256=HMAC_SHA256(payload, token)}。
 */
public final class EssCallbackVerifier {

    private EssCallbackVerifier() {
    }

    /**
     * 校验回调签名。
     *
     * @param payload 原始回调报文
     * @param contentSignature 请求头 Content-Signature
     * @param callbackToken 应用回调 Token
     * @return 是否通过
     */
    public static boolean verify(String payload, String contentSignature, String callbackToken) {
        if (!StringUtils.hasText(payload)
                || !StringUtils.hasText(contentSignature)
                || !StringUtils.hasText(callbackToken)) {
            return false;
        }
        String expected = "sha256=" + hmacSha256Hex(payload, callbackToken);
        return expected.equals(contentSignature);
    }

    /**
     * 校验回调签名，失败时抛业务异常。
     *
     * @param payload 原始回调报文
     * @param contentSignature 请求头 Content-Signature
     * @param callbackToken 应用回调 Token
     */
    public static void requireValid(String payload, String contentSignature, String callbackToken) {
        if (!verify(payload, contentSignature, callbackToken)) {
            throw new BusinessException(ResultCode.ESS_CALLBACK_INVALID, "电子签回调签名校验失败");
        }
    }

    /**
     * 计算 HMAC-SHA256 十六进制摘要。
     *
     * @param data 原文
     * @param key 密钥
     * @return hex 摘要
     */
    public static String hmacSha256Hex(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.ESS_CALLBACK_INVALID, "电子签回调签名计算失败", ex);
        }
    }
}
