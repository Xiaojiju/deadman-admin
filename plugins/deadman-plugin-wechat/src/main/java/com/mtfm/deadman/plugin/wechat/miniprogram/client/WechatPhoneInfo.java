package com.mtfm.deadman.plugin.wechat.miniprogram.client;

/**
 * 微信手机号信息。
 *
 * @param phoneNumber     含区号手机号
 * @param purePhoneNumber 纯手机号
 * @param countryCode     国家区号
 */
public record WechatPhoneInfo(String phoneNumber, String purePhoneNumber, String countryCode) {
}
