package com.mtfm.deadman.plugin.im.tencent.client.tls;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.Deflater;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 腾讯云 IM/TRTC TLS UserSig API v2 官方算法移植。
 * <p>
 * 来源：腾讯云 TLSSigAPIv2.java（GitHub 官方示例），仅保留 IM 所需的 UserSig 签发逻辑。
 */
public class TencentImTlsSigApiV2 {

    /** 腾讯云 IM SDKAppID */
    private final long sdkAppId;

    /** 腾讯云 IM SecretKey */
    private final String secretKey;

    /**
     * @param sdkAppId  SDKAppID
     * @param secretKey SecretKey
     */
    public TencentImTlsSigApiV2(long sdkAppId, String secretKey) {
        this.sdkAppId = sdkAppId;
        this.secretKey = secretKey;
    }

    /**
     * 签发 IM UserSig。
     *
     * @param userId        IM UserID，最长 32 字节
     * @param expireSeconds 有效期（秒）
     * @return UserSig，失败时返回空字符串
     */
    public String genUserSig(String userId, long expireSeconds) {
        return genUserSig(userId, expireSeconds, null);
    }

    private String genUserSig(String userId, long expireSeconds, byte[] userBuf) {
        long currentTime = System.currentTimeMillis() / 1000;

        String base64UserBuf = null;
        if (userBuf != null) {
            base64UserBuf = Base64.getEncoder().encodeToString(userBuf).replaceAll("\\s*", "");
        }

        String sig = hmacSha256(userId, currentTime, expireSeconds, base64UserBuf);
        if (sig.isEmpty()) {
            return "";
        }

        String sigDocJson = buildSigDocJson(userId, expireSeconds, currentTime, base64UserBuf, sig);

        Deflater compressor = new Deflater();
        compressor.setInput(sigDocJson.getBytes(StandardCharsets.UTF_8));
        compressor.finish();
        byte[] compressedBytes = new byte[2048];
        int compressedLength = compressor.deflate(compressedBytes);
        compressor.end();

        return new String(TencentImTlsSigBase64Url.encode(
                Arrays.copyOfRange(compressedBytes, 0, compressedLength)),
                StandardCharsets.UTF_8)
                .replaceAll("\\s*", "");
    }

    private String buildSigDocJson(
            String userId, long expireSeconds, long currentTime, String base64UserBuf, String sig) {
        StringBuilder builder = new StringBuilder(256);
        builder.append("{\"TLS.ver\":\"2.0\"");
        builder.append(",\"TLS.identifier\":\"").append(escapeJson(userId)).append('"');
        builder.append(",\"TLS.sdkappid\":").append(sdkAppId);
        builder.append(",\"TLS.expire\":").append(expireSeconds);
        builder.append(",\"TLS.time\":").append(currentTime);
        if (base64UserBuf != null) {
            builder.append(",\"TLS.userbuf\":\"").append(escapeJson(base64UserBuf)).append('"');
        }
        builder.append(",\"TLS.sig\":\"").append(escapeJson(sig)).append('"');
        builder.append('}');
        return builder.toString();
    }

    private String hmacSha256(String identifier, long currentTime, long expireSeconds, String base64UserBuf) {
        String contentToBeSigned = "TLS.identifier:" + identifier + "\n"
                + "TLS.sdkappid:" + sdkAppId + "\n"
                + "TLS.time:" + currentTime + "\n"
                + "TLS.expire:" + expireSeconds + "\n"
                + (base64UserBuf != null ? "TLS.userbuf:" + base64UserBuf + "\n" : "");
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] byteSig = mac.doFinal(contentToBeSigned.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(byteSig).replaceAll("\\s*", "");
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            return "";
        }
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
