package com.mtfm.deadman.plugin.wechat.web.client;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.wechat.common.WechatRestClientSupport;
import com.mtfm.deadman.plugin.wechat.web.config.WechatWebPluginProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

/**
 * 默认微信网页扫码登录 API 客户端实现。
 */
@Component
@RequiredArgsConstructor
public class DefaultWechatWebApiClient implements WechatWebApiClient {

    private final WechatWebPluginProperties properties;
    private final WechatRestClientSupport restClientSupport;

    /**
     * 使用授权 code 换取 OAuth2 access_token 与 openid。
     *
     * @param code 微信授权临时凭证
     * @return OAuth2 令牌结果
     */
    @Override
    public WechatOAuth2TokenResult oauth2AccessToken(String code) {
        requireConfigured();
        String url = UriComponentsBuilder.fromUriString(properties.getApiBaseUrl() + "/sns/oauth2/access_token")
                .queryParam("appid", properties.getAppId())
                .queryParam("secret", properties.getAppSecret())
                .queryParam("code", code)
                .queryParam("grant_type", "authorization_code")
                .toUriString();
        JsonNode body = restClientSupport.getForJson(url);
        restClientSupport.assertWechatSuccess(body, "微信网页授权失败");
        String openid = restClientSupport.textValue(body, "openid");
        if (!StringUtils.hasText(openid)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "微信网页授权未返回 openid");
        }
        int expiresIn = body.has("expires_in") ? body.get("expires_in").asInt(7200) : 7200;
        return new WechatOAuth2TokenResult(
                restClientSupport.textValue(body, "access_token"),
                expiresIn,
                restClientSupport.textValue(body, "refresh_token"),
                openid,
                restClientSupport.textValue(body, "scope"),
                restClientSupport.textValue(body, "unionid"));
    }

    /**
     * 拉取授权用户资料。
     *
     * @param accessToken 网页授权 access_token
     * @param openid      用户 openid
     * @return 用户资料
     */
    @Override
    public WechatWebUserInfo getUserInfo(String accessToken, String openid) {
        requireConfigured();
        String url = UriComponentsBuilder.fromUriString(properties.getApiBaseUrl() + "/sns/userinfo")
                .queryParam("access_token", accessToken)
                .queryParam("openid", openid)
                .queryParam("lang", "zh_CN")
                .toUriString();
        JsonNode body = restClientSupport.getForJson(url);
        restClientSupport.assertWechatSuccess(body, "获取微信用户资料失败");
        String resolvedOpenid = restClientSupport.textValue(body, "openid");
        if (!StringUtils.hasText(resolvedOpenid)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "微信未返回用户 openid");
        }
        return new WechatWebUserInfo(
                resolvedOpenid,
                restClientSupport.textValue(body, "nickname"),
                restClientSupport.textValue(body, "headimgurl"),
                restClientSupport.textValue(body, "unionid"));
    }

    /**
     * 检查网站应用 AppId/AppSecret/redirectUri 是否配置。
     */
    private void requireConfigured() {
        if (!StringUtils.hasText(properties.getAppId()) || !StringUtils.hasText(properties.getAppSecret())) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "未配置微信网页应用 AppId/AppSecret");
        }
    }
}
