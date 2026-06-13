package com.mtfm.deadman.plugin.wechat.miniprogram.client;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.wechat.miniprogram.config.WechatMiniprogramPluginProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

/**
 * 默认微信 API 客户端实现（RestClient 调用官方接口）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultWechatApiClient implements WechatApiClient {

    private final WechatMiniprogramPluginProperties properties;
    private final WechatAccessTokenHolder accessTokenHolder;
    private final JsonMapper jsonMapper;
    private final RestClient restClient = RestClient.create();

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
        JsonNode body = getForJson(url);
        assertWechatSuccess(body, "微信登录失败");
        String openid = textValue(body, "openid");
        if (!StringUtils.hasText(openid)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "微信登录未返回 openid");
        }
        return new WechatCode2SessionResult(openid, textValue(body, "session_key"), textValue(body, "unionid"));
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
        JsonNode body = postForJson(url, Map.of("code", phoneCode));
        assertWechatSuccess(body, "获取微信手机号失败");
        JsonNode phoneInfo = body.get("phone_info");
        if (phoneInfo == null || phoneInfo.isNull()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "微信未返回手机号信息");
        }
        return new WechatPhoneInfo(
                textValue(phoneInfo, "phoneNumber"),
                textValue(phoneInfo, "purePhoneNumber"),
                textValue(phoneInfo, "countryCode"));
    }

    private String fetchAccessToken() {
        requireConfigured();
        String url = UriComponentsBuilder.fromUriString(properties.getApiBaseUrl() + "/cgi-bin/token")
                .queryParam("grant_type", "client_credential")
                .queryParam("appid", properties.getAppId())
                .queryParam("secret", properties.getAppSecret())
                .toUriString();
        JsonNode body = getForJson(url);
        assertWechatSuccess(body, "获取微信 access_token 失败");
        String token = textValue(body, "access_token");
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "微信 access_token 为空");
        }
        int expiresIn = body.has("expires_in") ? body.get("expires_in").asInt(7200) : 7200;
        accessTokenHolder.updateExpiresIn(expiresIn);
        return token;
    }

    private JsonNode getForJson(String url) {
        String rawBody = restClient.get().uri(url).retrieve().body(String.class);
        return parseJsonBody(rawBody);
    }

    private JsonNode postForJson(String url, Object requestBody) {
        String rawBody = restClient.post().uri(url).body(requestBody).retrieve().body(String.class);
        return parseJsonBody(rawBody);
    }

    /**
     * 解析微信 API 响应体。微信部分接口以 text/plain 返回 JSON，不能直接反序列化为 JsonNode。
     *
     * @param rawBody 原始响应字符串
     * @return 解析后的 JSON 节点
     */
    private JsonNode parseJsonBody(String rawBody) {
        if (!StringUtils.hasText(rawBody)) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "微信 API 响应为空");
        }
        try {
            return jsonMapper.readTree(rawBody);
        } catch (Exception ex) {
            log.warn("微信 API 响应解析失败: {}", rawBody, ex);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "微信 API 响应格式错误");
        }
    }

    private void requireConfigured() {
        if (!StringUtils.hasText(properties.getAppId()) || !StringUtils.hasText(properties.getAppSecret())) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "未配置微信小程序 AppId/AppSecret");
        }
    }

    private void assertWechatSuccess(JsonNode body, String defaultMessage) {
        if (body == null) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, defaultMessage);
        }
        if (body.has("errcode") && body.get("errcode").asInt() != 0) {
            String message = body.has("errmsg") ? body.get("errmsg").asString() : defaultMessage;
            log.warn("微信 API 失败: {}", message);
            throw new BusinessException(ResultCode.BAD_REQUEST, message);
        }
    }

    private String textValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }
}
