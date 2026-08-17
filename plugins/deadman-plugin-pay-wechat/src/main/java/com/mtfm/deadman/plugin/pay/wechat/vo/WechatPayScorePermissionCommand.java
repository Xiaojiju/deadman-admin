package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 微信支付分授权命令。
 *
 * @param appId             商户 AppId
 * @param serviceId         服务 ID
 * @param authorizationCode 授权协议号（可选）
 * @param openid            用户 openid（可选）
 * @param notifyUrl         授权回调 URL（可选）
 */
public record WechatPayScorePermissionCommand(
        String appId, String serviceId, String authorizationCode, String openid, String notifyUrl) {
}
