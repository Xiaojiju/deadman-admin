package com.mtfm.deadman.plugin.wechat.miniprogram.vo;

/**
 * 微信 getPhoneNumber code 换号结果（明文，供前端回填；不落库）。
 *
 * @param phone       纯手机号
 * @param countryCode 国家区号，可为空
 */
public record WechatResolvedPhoneVO(String phone, String countryCode) {
}
