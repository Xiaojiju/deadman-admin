package com.mtfm.deadman.plugin.im.tencent.client;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.Deflater;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.im.tencent.config.ImTencentPluginProperties;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

/**
 * 腾讯云 IM TLS UserSig API v2 生成器。
 */
@RequiredArgsConstructor
public class TencentImUserSigGenerator {

    private final ImTencentPluginProperties properties;
    private final JsonMapper jsonMapper;

    /**
     * 生成 UserSig。
     *
     * @param imUserId      IM UserID
     * @param expireSeconds 有效期（秒）
     * @return UserSig
     */
    public String generate(String imUserId, long expireSeconds) {
        if (!StringUtils.hasText(imUserId)) {
            throw new BusinessException(ResultCode.IM_CONFIG_INVALID, "IM UserID 不能为空");
        }
        properties.requireProductionConfig();
        long currentTime = System.currentTimeMillis() / 1000;
        try {
            String signature = hmacSha256(imUserId, currentTime, expireSeconds, null);
            Map<String, Object> sigDoc = new LinkedHashMap<>();
            sigDoc.put("TLS.ver", "2.0");
            sigDoc.put("TLS.identifier", imUserId);
            sigDoc.put("TLS.sdkappid", properties.getSdkAppId());
            sigDoc.put("TLS.expire", expireSeconds);
            sigDoc.put("TLS.time", currentTime);
            sigDoc.put("TLS.sig", signature);
            byte[] jsonBytes = jsonMapper.writeValueAsBytes(sigDoc);
            byte[] compressed = deflate(jsonBytes);
            return base64UrlEncode(compressed);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.IM_CONFIG_INVALID, "UserSig 生成失败：" + ex.getMessage());
        }
    }

    private String hmacSha256(String identifier, long currentTime, long expireSeconds, String base64UserBuf)
            throws NoSuchAlgorithmException, InvalidKeyException {
        StringBuilder content = new StringBuilder();
        content.append("TLS.identifier:").append(identifier).append('\n');
        content.append("TLS.sdkappid:").append(properties.getSdkAppId()).append('\n');
        content.append("TLS.time:").append(currentTime).append('\n');
        content.append("TLS.expire:").append(expireSeconds).append('\n');
        if (StringUtils.hasText(base64UserBuf)) {
            content.append("TLS.userbuf:").append(base64UserBuf).append('\n');
        }
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(properties.getSecretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(content.toString().getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private static byte[] deflate(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        deflater.end();
        return outputStream.toByteArray();
    }

    private static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(data)
                .replace('+', '*')
                .replace('/', '-')
                .replace('=', '_');
    }
}
