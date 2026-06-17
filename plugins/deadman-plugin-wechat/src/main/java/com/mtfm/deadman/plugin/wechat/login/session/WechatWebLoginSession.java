package com.mtfm.deadman.plugin.wechat.login.session;

import com.mtfm.deadman.plugin.wechat.login.WechatLoginKinds;

/**
 * 微信网页扫码登录会话，扩展 {@link WechatLoginSession} 模板。
 *
 * @param openid      微信 openid
 * @param unionid     微信 unionid
 * @param nickname    微信昵称
 * @param avatarUrl   微信头像 URL
 * @param accessToken 网页授权 access_token（仅供后续扩展，绑定流程不持久化）
 */
public record WechatWebLoginSession(
        String openid, String unionid, String nickname, String avatarUrl, String accessToken)
        implements WechatLoginSession {

    @Override
    public String loginKind() {
        return WechatLoginKinds.WEB;
    }

    @Override
    public String oauthProvider() {
        return WechatLoginKinds.WEB;
    }
}
