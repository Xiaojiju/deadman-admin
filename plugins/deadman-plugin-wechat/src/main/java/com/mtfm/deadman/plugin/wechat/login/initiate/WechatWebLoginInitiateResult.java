package com.mtfm.deadman.plugin.wechat.login.initiate;

import com.mtfm.deadman.plugin.wechat.login.WechatLoginKinds;

/**
 * 微信网页扫码登录发起结果，扩展 {@link WechatLoginInitiateResult} 模板。
 *
 * @param authorizeUrl          微信扫码授权页 URL
 * @param state                 OAuth state，登录时需原样回传
 * @param stateExpiresInSeconds state 有效秒数
 */
public record WechatWebLoginInitiateResult(String authorizeUrl, String state, long stateExpiresInSeconds)
        implements WechatLoginInitiateResult {

    @Override
    public String loginKind() {
        return WechatLoginKinds.WEB;
    }
}
