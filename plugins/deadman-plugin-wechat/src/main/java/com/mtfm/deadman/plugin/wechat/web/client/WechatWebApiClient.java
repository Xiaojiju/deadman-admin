package com.mtfm.deadman.plugin.wechat.web.client;

/**
 * 微信网页扫码登录 API 客户端。
 */
public interface WechatWebApiClient {

    /**
     * 使用授权 code 换取 OAuth2 access_token 与 openid。
     *
     * @param code 微信授权临时凭证
     * @return OAuth2 令牌结果
     */
    WechatOAuth2TokenResult oauth2AccessToken(String code);

    /**
     * 拉取授权用户资料（需 snsapi_login 授权）。
     *
     * @param accessToken 网页授权 access_token
     * @param openid      用户 openid
     * @return 用户资料
     */
    WechatWebUserInfo getUserInfo(String accessToken, String openid);
}
