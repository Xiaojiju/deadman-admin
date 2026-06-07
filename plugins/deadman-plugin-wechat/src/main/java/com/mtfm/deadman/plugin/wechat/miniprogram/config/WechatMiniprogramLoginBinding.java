package com.mtfm.deadman.plugin.wechat.miniprogram.config;

/**
 * 微信小程序登录绑定：将微信登录注册到指定用户体系组。
 *
 * @param groupId          登录 Provider 组标识，如 client、admin
 * @param loginPathSegment 登录路径段，为空时使用插件默认值
 */
public record WechatMiniprogramLoginBinding(String groupId, String loginPathSegment) {
}
