package com.mtfm.deadman.plugin.wechat.login.session;

import com.mtfm.deadman.plugin.wechat.login.WechatLoginKinds;

/**
 * 微信小程序登录会话，扩展 {@link WechatLoginSession} 模板。
 *
 * @param openid     微信 openid
 * @param unionid    微信 unionid
 * @param sessionKey 微信 session_key
 */
public record WechatMiniprogramLoginSession(String openid, String unionid, String sessionKey)
        implements WechatLoginSession {

    @Override
    public String loginKind() {
        return WechatLoginKinds.MINIPROGRAM;
    }

    @Override
    public String oauthProvider() {
        return WechatLoginKinds.MINIPROGRAM;
    }

    @Override
    public String nickname() {
        return null;
    }

    @Override
    public String avatarUrl() {
        return null;
    }
}
