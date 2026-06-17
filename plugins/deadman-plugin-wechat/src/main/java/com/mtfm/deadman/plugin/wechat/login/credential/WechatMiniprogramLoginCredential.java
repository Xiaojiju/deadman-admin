package com.mtfm.deadman.plugin.wechat.login.credential;

import com.mtfm.deadman.plugin.wechat.login.WechatLoginKinds;

/**
 * 微信小程序登录凭证（wx.login 返回的 code）。
 *
 * @param code 临时登录凭证
 */
public record WechatMiniprogramLoginCredential(String code) implements WechatLoginCredential {

    @Override
    public String loginKind() {
        return WechatLoginKinds.MINIPROGRAM;
    }
}
