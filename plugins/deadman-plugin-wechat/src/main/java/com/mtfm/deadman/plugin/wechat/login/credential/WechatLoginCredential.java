package com.mtfm.deadman.plugin.wechat.login.credential;

/**
 * 微信登录凭证模板，各登录方式实现此接口并扩展特有字段。
 */
public sealed interface WechatLoginCredential permits WechatMiniprogramLoginCredential, WechatWebLoginCredential {

    /**
     * 登录方式标识，如 wechat-miniprogram、wechat-web。
     *
     * @return 登录方式标识
     */
    String loginKind();
}
