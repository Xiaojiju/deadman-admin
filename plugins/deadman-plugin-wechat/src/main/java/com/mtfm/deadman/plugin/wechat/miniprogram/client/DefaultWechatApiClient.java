package com.mtfm.deadman.plugin.wechat.miniprogram.client;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.wechat.common.WechatRestClientSupport;
import com.mtfm.deadman.plugin.wechat.miniprogram.config.WechatMiniprogramPluginProperties;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatFaceCertInfo;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatUnlimitedQrCodeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 默认微信 API 客户端实现（RestClient 调用官方接口）。
 */
@Component
@RequiredArgsConstructor
public class DefaultWechatApiClient implements WechatApiClient {

    private final WechatMiniprogramPluginProperties properties;
    private final WechatAccessTokenHolder accessTokenHolder;
    private final WechatRestClientSupport restClientSupport;
    private final JsonMapper jsonMapper;

    /**
     * 使用 wx.login code 换取 openid。
     *
     * @param jsCode 临时登录凭证
     * @return 会话信息
     */
    @Override
    public WechatCode2SessionResult code2Session(String jsCode) {
        requireConfigured();
        String url = UriComponentsBuilder.fromUriString(properties.getApiBaseUrl() + "/sns/jscode2session")
                .queryParam("appid", properties.getAppId())
                .queryParam("secret", properties.getAppSecret())
                .queryParam("js_code", jsCode)
                .queryParam("grant_type", "authorization_code")
                .toUriString();
        JsonNode body = restClientSupport.getForJson(url);
        restClientSupport.assertWechatSuccess(body, "微信登录失败");
        String openid = restClientSupport.textValue(body, "openid");
        if (!StringUtils.hasText(openid)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "微信登录未返回 openid");
        }
        return new WechatCode2SessionResult(
                openid,
                restClientSupport.textValue(body, "session_key"),
                // 用户在开放平台的唯一标识符，若当前小程序已绑定到微信开放平台帐号下会返回
                // 「https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/union-id.html」
                restClientSupport.textValue(body, "unionid"));
    }

    /**
     * 获取 access_token（带缓存）。
     *
     * @return access_token
     */
    @Override
    public String getAccessToken() {
        return accessTokenHolder.getAccessToken(this::fetchAccessToken);
    }

    /**
     * 使用手机号 code 换取用户手机号。
     *
     * @param phoneCode 手机号动态令牌
     * @return 手机号信息
     */
    @Override
    public WechatPhoneInfo getPhoneNumber(String phoneCode) {
        String accessToken = getAccessToken();
        String url = properties.getApiBaseUrl() + "/wxa/business/getuserphonenumber?access_token=" + accessToken;
        JsonNode body = restClientSupport.postForJson(url, Map.of("code", phoneCode));
        restClientSupport.assertWechatSuccess(body, "获取微信手机号失败");
        JsonNode phoneInfo = body.get("phone_info");
        if (phoneInfo == null || phoneInfo.isNull()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "微信未返回手机号信息");
        }
        return new WechatPhoneInfo(
                restClientSupport.textValue(phoneInfo, "phoneNumber"),
                restClientSupport.textValue(phoneInfo, "purePhoneNumber"),
                restClientSupport.textValue(phoneInfo, "countryCode"));
    }

    /**
     * 获取用户人脸核身会话唯一标识 verifyId。
     *
     * @param outSeqNo 业务流水号
     * @param certInfo 用户身份信息
     * @param openid   用户 openid
     * @return verifyId 及有效期
     */
    @Override
    public WechatGetVerifyIdResult getVerifyId(String outSeqNo, WechatFaceCertInfo certInfo, String openid) {
        String accessToken = getAccessToken();
        String url = properties.getApiBaseUrl() + "/cityservice/face/identify/getverifyid?access_token=" + accessToken;
        JsonNode body = restClientSupport.postForJson(
                url,
                Map.of(
                        "out_seq_no",
                        outSeqNo,
                        "cert_info",
                        Map.of(
                                "cert_type", certInfo.certType(),
                                "cert_name", certInfo.certName(),
                                "cert_no", certInfo.certNo()),
                        "openid",
                        openid));
        restClientSupport.assertWechatSuccess(body, "获取人脸核身 verifyId 失败");
        String verifyId = restClientSupport.textValue(body, "verify_id");
        if (!StringUtils.hasText(verifyId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "微信未返回 verify_id");
        }
        int expiresIn = body.has("expires_in") ? body.get("expires_in").asInt(3600) : 3600;
        return new WechatGetVerifyIdResult(verifyId, expiresIn);
    }

    /**
     * 查询用户人脸核身真实验证结果。
     *
     * @param verifyId 人脸核身会话唯一标识
     * @param outSeqNo 业务流水号
     * @param certHash 证件信息摘要
     * @param openid   用户 openid
     * @return 核身结果码
     */
    @Override
    public WechatQueryVerifyInfoResult queryVerifyInfo(
            String verifyId, String outSeqNo, String certHash, String openid) {
        String accessToken = getAccessToken();
        String url = properties.getApiBaseUrl() + "/cityservice/face/identify/queryverifyinfo?access_token="
                + accessToken;
        JsonNode body = restClientSupport.postForJson(
                url,
                Map.of(
                        "verify_id", verifyId,
                        "out_seq_no", outSeqNo,
                        "cert_hash", certHash,
                        "openid", openid));
        restClientSupport.assertWechatSuccess(body, "查询人脸核身结果失败");
        int verifyRet = body.has("verify_ret") ? body.get("verify_ret").asInt() : 0;
        return new WechatQueryVerifyInfoResult(verifyRet);
    }

    /**
     * 获取不限制的小程序码。
     *
     * @param request 小程序码参数
     * @return 图片二进制
     */
    @Override
    public byte[] getUnlimitedQrCode(WechatUnlimitedQrCodeRequest request) {
        if (request == null || !StringUtils.hasText(request.scene())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "小程序码 scene 不能为空");
        }
        if (request.scene().length() > 32) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "小程序码 scene 最多 32 个字符");
        }
        String accessToken = getAccessToken();
        String url = properties.getApiBaseUrl() + "/wxa/getwxacodeunlimit?access_token=" + accessToken;
        return restClientSupport.postForBytes(url, buildUnlimitedQrCodeBody(request), "获取小程序码失败");
    }

    /**
     * 组装 getwxacodeunlimit 请求体 JSON 字符串，仅写入非空可选字段。
     *
     * @param request 请求参数
     * @return JSON 请求体字符串
     */
    private String buildUnlimitedQrCodeBody(WechatUnlimitedQrCodeRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("scene", request.scene());
        if (StringUtils.hasText(request.page())) {
            body.put("page", request.page());
        }
        if (request.checkPath() != null) {
            body.put("check_path", request.checkPath());
        }
        if (StringUtils.hasText(request.envVersion())) {
            body.put("env_version", request.envVersion());
        }
        if (request.width() != null) {
            body.put("width", request.width());
        }
        if (request.autoColor() != null) {
            body.put("auto_color", request.autoColor());
        }
        if (request.lineColor() != null) {
            body.put(
                    "line_color",
                    Map.of(
                            "r", request.lineColor().r(),
                            "g", request.lineColor().g(),
                            "b", request.lineColor().b()));
        }
        if (request.hyaline() != null) {
            body.put("is_hyaline", request.hyaline());
        }
        try {
            return jsonMapper.writeValueAsString(body);
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "组装小程序码请求体失败");
        }
    }

    /**
     * 获取微信 access_token。
     *
     * @return access_token
     */
    private String fetchAccessToken() {
        requireConfigured();
        String url = UriComponentsBuilder.fromUriString(properties.getApiBaseUrl() + "/cgi-bin/token")
                .queryParam("grant_type", "client_credential")
                .queryParam("appid", properties.getAppId())
                .queryParam("secret", properties.getAppSecret())
                .toUriString();
        JsonNode body = restClientSupport.getForJson(url);
        restClientSupport.assertWechatSuccess(body, "获取微信 access_token 失败");
        String token = restClientSupport.textValue(body, "access_token");
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "微信 access_token 为空");
        }
        int expiresIn = body.has("expires_in") ? body.get("expires_in").asInt(7200) : 7200;
        accessTokenHolder.updateExpiresIn(expiresIn);
        return token;
    }

    /**
     * 检查微信小程序 AppId/AppSecret 是否配置。
     */
    private void requireConfigured() {
        if (!StringUtils.hasText(properties.getAppId()) || !StringUtils.hasText(properties.getAppSecret())) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "未配置微信小程序 AppId/AppSecret");
        }
    }
}
