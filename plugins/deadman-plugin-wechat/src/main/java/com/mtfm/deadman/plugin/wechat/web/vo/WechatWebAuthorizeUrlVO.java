package com.mtfm.deadman.plugin.wechat.web.vo;

/**
 * 微信网页扫码登录授权地址。
 *
 * @param authorizeUrl          微信开放平台扫码授权页 URL，前端用于展示二维码或跳转
 * @param state                 OAuth state，登录时需原样回传
 * @param stateExpiresInSeconds state 有效秒数
 */
public record WechatWebAuthorizeUrlVO(String authorizeUrl, String state, long stateExpiresInSeconds) {
}
