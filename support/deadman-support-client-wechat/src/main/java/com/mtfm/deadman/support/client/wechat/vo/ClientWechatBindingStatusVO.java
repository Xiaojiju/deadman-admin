package com.mtfm.deadman.support.client.wechat.vo;

/**
 * 当前用户微信小程序 OAuth 绑定状态。
 *
 * @param bound 是否已绑定微信小程序（存在 wechat-miniprogram 的 OAuth 账号）
 */
public record ClientWechatBindingStatusVO(boolean bound) {
}
