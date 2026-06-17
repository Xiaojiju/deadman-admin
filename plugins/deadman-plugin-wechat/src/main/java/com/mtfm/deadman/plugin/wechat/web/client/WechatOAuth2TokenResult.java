package com.mtfm.deadman.plugin.wechat.web.client;

/**
 * 微信网页 OAuth2 access_token 换取结果。
 *
 * @param accessToken  接口调用凭证
 * @param expiresIn    过期秒数
 * @param refreshToken 刷新令牌
 * @param openid       用户唯一标识
 * @param scope        授权作用域
 * @param unionid      开放平台统一标识，未绑定时为 null
 */
public record WechatOAuth2TokenResult(
                String accessToken, int expiresIn, String refreshToken, String openid, String scope, String unionid) {
}
