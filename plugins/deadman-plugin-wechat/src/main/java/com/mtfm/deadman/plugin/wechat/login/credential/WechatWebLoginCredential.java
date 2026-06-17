package com.mtfm.deadman.plugin.wechat.login.credential;

import com.mtfm.deadman.plugin.wechat.login.WechatLoginKinds;

/**
 * 微信网页扫码登录凭证（授权回调 code 与 state）。
 *
 * @param code  微信授权临时凭证
 * @param state OAuth 防 CSRF 状态值
 */
public record WechatWebLoginCredential(String code, String state) implements WechatLoginCredential {

    @Override
    public String loginKind() {
        return WechatLoginKinds.WEB;
    }
}
