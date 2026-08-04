package com.mtfm.deadman.plugin.im.tencent.client.tls;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.Deflater;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;

/**
 * 腾讯云 IM/TRTC TLS UserSig API v2（官方 {@code TLSSigAPIv2} 移植）。
 * <p>
 * 来源：腾讯云官方示例 {@code com.tencentyun.TLSSigAPIv2}，保持算法与字段一致。
 */
public class TencentImTlsSigApiV2 {

    /** 腾讯云 SDKAppID */
    private final long sdkAppId;

    /** 腾讯云 SecretKey */
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
     * 签发 TRTC / IM 所需的 UserSig。
     *
     * @param userId 用户 ID，最长 32 字节，允许字母数字下划线与连字符
     * @param expire UserSig 过期时间（秒），如 86400 表示一天后失效
     * @return UserSig；签名失败时返回空字符串
     */
    public String genUserSig(String userId, long expire) {
        return genUserSig(userId, expire, null);
    }

    /**
     * 签发带房间权限控制的 PrivateMapKey（需配合 UserSig 使用）。
     *
     * @param userId       用户 ID
     * @param expire       过期时间（秒）
     * @param roomId       房间号
     * @param privilegeMap 权限位
     * @return 带 userbuf 的签名
     */
    public String genPrivateMapKey(String userId, long expire, long roomId, long privilegeMap) {
        byte[] userBuf = genUserBuf(userId, roomId, expire, privilegeMap, 0, "");
        return genUserSig(userId, expire, userBuf);
    }

    /**
     * 签发字符串房间号版本的 PrivateMapKey。
     *
     * @param userId       用户 ID
     * @param expire       过期时间（秒）
     * @param roomStr      字符串房间号
     * @param privilegeMap 权限位
     * @return 带 userbuf 的签名
     */
    public String genPrivateMapKeyWithStringRoomId(String userId, long expire, String roomStr, long privilegeMap) {
        byte[] userBuf = genUserBuf(userId, 0, expire, privilegeMap, 0, roomStr);
        return genUserSig(userId, expire, userBuf);
    }

    private String hmacSha256(String identifier, long currTime, long expire, String base64UserBuf) {
        String contentToBeSigned = "TLS.identifier:" + identifier + "\n"
                + "TLS.sdkappid:" + sdkAppId + "\n"
                + "TLS.time:" + currTime + "\n"
                + "TLS.expire:" + expire + "\n";
        if (base64UserBuf != null) {
            contentToBeSigned += "TLS.userbuf:" + base64UserBuf + "\n";
        }
        try {
            byte[] byteKey = secretKey.getBytes(StandardCharsets.UTF_8);
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, "HmacSHA256");
            hmac.init(keySpec);
            byte[] byteSig = hmac.doFinal(contentToBeSigned.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(byteSig).replaceAll("\\s*", "");
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            return "";
        }
    }

    private String genUserSig(String userId, long expire, byte[] userBuf) {
        long currTime = System.currentTimeMillis() / 1000;

        JSONObject sigDoc = new JSONObject();
        sigDoc.put("TLS.ver", "2.0");
        sigDoc.put("TLS.identifier", userId);
        sigDoc.put("TLS.sdkappid", sdkAppId);
        sigDoc.put("TLS.expire", expire);
        sigDoc.put("TLS.time", currTime);

        String base64UserBuf = null;
        if (userBuf != null) {
            base64UserBuf = Base64.getEncoder().encodeToString(userBuf).replaceAll("\\s*", "");
            sigDoc.put("TLS.userbuf", base64UserBuf);
        }
        String sig = hmacSha256(userId, currTime, expire, base64UserBuf);
        if (sig.isEmpty()) {
            return "";
        }
        sigDoc.put("TLS.sig", sig);

        Deflater compressor = new Deflater();
        compressor.setInput(sigDoc.toString().getBytes(StandardCharsets.UTF_8));
        compressor.finish();
        byte[] compressedBytes = new byte[2048];
        int compressedBytesLength = compressor.deflate(compressedBytes);
        compressor.end();
        return new String(TencentImTlsSigBase64Url.encode(
                Arrays.copyOfRange(compressedBytes, 0, compressedBytesLength)), StandardCharsets.UTF_8)
                .replaceAll("\\s*", "");
    }

    /**
     * 生成 PrivateMapKey 所需的 userbuf（网络字节序）。
     *
     * @param account        账号
     * @param dwAuthId       房间号（数字）
     * @param dwExpTime      有效期（秒）
     * @param dwPrivilegeMap 权限位
     * @param dwAccountType  账号类型
     * @param roomStr        字符串房间号，可为空
     * @return userbuf 字节
     */
    public byte[] genUserBuf(String account, long dwAuthId, long dwExpTime, long dwPrivilegeMap, long dwAccountType,
            String roomStr) {
        int accountLength = account.length();
        int roomStrLength = roomStr.length();
        int offset = 0;
        int bufLength = 1 + 2 + accountLength + 20;
        if (roomStrLength > 0) {
            bufLength = bufLength + 2 + roomStrLength;
        }
        byte[] userBuf = new byte[bufLength];

        // cVer
        if (roomStrLength > 0) {
            userBuf[offset++] = 1;
        } else {
            userBuf[offset++] = 0;
        }

        // wAccountLen
        userBuf[offset++] = (byte) ((accountLength & 0xFF00) >> 8);
        userBuf[offset++] = (byte) (accountLength & 0x00FF);

        // account
        for (; offset < 3 + accountLength; ++offset) {
            userBuf[offset] = (byte) account.charAt(offset - 3);
        }

        // dwSdkAppid
        userBuf[offset++] = (byte) ((sdkAppId & 0xFF000000) >> 24);
        userBuf[offset++] = (byte) ((sdkAppId & 0x00FF0000) >> 16);
        userBuf[offset++] = (byte) ((sdkAppId & 0x0000FF00) >> 8);
        userBuf[offset++] = (byte) (sdkAppId & 0x000000FF);

        // dwAuthId
        userBuf[offset++] = (byte) ((dwAuthId & 0xFF000000) >> 24);
        userBuf[offset++] = (byte) ((dwAuthId & 0x00FF0000) >> 16);
        userBuf[offset++] = (byte) ((dwAuthId & 0x0000FF00) >> 8);
        userBuf[offset++] = (byte) (dwAuthId & 0x000000FF);

        // expire = 当前时间 + 有效期
        long currTime = System.currentTimeMillis() / 1000;
        long expire = currTime + dwExpTime;
        userBuf[offset++] = (byte) ((expire & 0xFF000000) >> 24);
        userBuf[offset++] = (byte) ((expire & 0x00FF0000) >> 16);
        userBuf[offset++] = (byte) ((expire & 0x0000FF00) >> 8);
        userBuf[offset++] = (byte) (expire & 0x000000FF);

        // dwPrivilegeMap
        userBuf[offset++] = (byte) ((dwPrivilegeMap & 0xFF000000) >> 24);
        userBuf[offset++] = (byte) ((dwPrivilegeMap & 0x00FF0000) >> 16);
        userBuf[offset++] = (byte) ((dwPrivilegeMap & 0x0000FF00) >> 8);
        userBuf[offset++] = (byte) (dwPrivilegeMap & 0x000000FF);

        // dwAccountType
        userBuf[offset++] = (byte) ((dwAccountType & 0xFF000000) >> 24);
        userBuf[offset++] = (byte) ((dwAccountType & 0x00FF0000) >> 16);
        userBuf[offset++] = (byte) ((dwAccountType & 0x0000FF00) >> 8);
        userBuf[offset++] = (byte) (dwAccountType & 0x000000FF);

        if (roomStrLength > 0) {
            userBuf[offset++] = (byte) ((roomStrLength & 0xFF00) >> 8);
            userBuf[offset++] = (byte) (roomStrLength & 0x00FF);
            for (; offset < bufLength; ++offset) {
                userBuf[offset] = (byte) roomStr.charAt(offset - (bufLength - roomStrLength));
            }
        }
        return userBuf;
    }
}
