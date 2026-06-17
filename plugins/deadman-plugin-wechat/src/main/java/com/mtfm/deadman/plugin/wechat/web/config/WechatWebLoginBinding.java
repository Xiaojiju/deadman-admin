package com.mtfm.deadman.plugin.wechat.web.config;

/**
 * 微信网页扫码登录绑定配置项。
 *
 * @param groupId          登录 Provider 组标识
 * @param loginPathSegment 登录路径段，为空时使用插件默认值 wechat-web
 */
public record WechatWebLoginBinding(String groupId, String loginPathSegment) {
}
