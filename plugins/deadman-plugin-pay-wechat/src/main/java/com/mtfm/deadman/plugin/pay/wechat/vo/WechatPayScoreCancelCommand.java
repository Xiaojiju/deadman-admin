package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 微信支付分取消服务订单命令。
 *
 * @param appId      商户 AppId
 * @param serviceId  服务 ID
 * @param outOrderNo 商户服务订单号
 * @param reason     取消原因
 */
public record WechatPayScoreCancelCommand(
        String appId, String serviceId, String outOrderNo, String reason) {
}
