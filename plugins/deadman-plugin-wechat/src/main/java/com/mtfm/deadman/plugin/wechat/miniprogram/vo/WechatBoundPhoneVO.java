package com.mtfm.deadman.plugin.wechat.miniprogram.vo;

/**
 * 当前用户已绑定的微信手机号（脱敏展示）。
 *
 * @param phone 已绑定手机号（脱敏）；未绑定时为 null
 */
public record WechatBoundPhoneVO(String phone) {
}
