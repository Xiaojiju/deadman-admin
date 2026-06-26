package com.mtfm.deadman.plugin.wechat.miniprogram.support;

import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatFaceCertInfo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 微信人脸核身 cert_hash 计算支持。
 * <p>
 * 规则见
 * <a href="https://developers.weixin.qq.com/miniprogram/dev/server/API/face/api_queryverifyinfo.html">queryVerifyInfo</a>。
 */
public final class WechatFaceCertHashSupport {

    private WechatFaceCertHashSupport() {
    }

    /**
     * 根据证件信息生成 cert_hash。
     *
     * @param certInfo 证件信息
     * @return SHA256 十六进制小写摘要
     */
    public static String computeCertHash(WechatFaceCertInfo certInfo) {
        String certTypeEncoded = encodeBase64(certInfo.certType());
        String certNameEncoded = encodeBase64(certInfo.certName());
        String certNoEncoded = encodeBase64(certInfo.certNo());
        String payload = "cert_type="
                + certTypeEncoded
                + "&cert_name="
                + certNameEncoded
                + "&cert_no="
                + certNoEncoded;
        return sha256Hex(payload);
    }

    /**
     * 对字符串进行 UTF-8 标准 Base64 编码。
     *
     * @param value 原始字符串
     * @return Base64 编码结果
     */
    private static String encodeBase64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算字符串的 SHA256 十六进制小写摘要。
     *
     * @param payload 待摘要字符串
     * @return SHA256 十六进制小写结果
     */
    private static String sha256Hex(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte item : hash) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 算法不可用", ex);
        }
    }
}
