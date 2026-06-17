package com.mtfm.deadman.plugin.wechat.login.initiate;

/**
 * 微信登录发起结果模板，用于需要预生成参数的场景（如网页扫码授权 URL）。
 */
public sealed interface WechatLoginInitiateResult permits WechatWebLoginInitiateResult {

    /**
     * 登录方式标识。
     *
     * @return 登录方式标识
     */
    String loginKind();
}
