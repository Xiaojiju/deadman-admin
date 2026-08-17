package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 微信支付分通知/查询统一解析结果。
 *
 * @param outOrderNo        商户服务订单号
 * @param channelOrderId    微信服务订单号
 * @param state             订单状态
 * @param stateDescription  状态描述（可选）
 * @param totalAmountCents  总金额（分，可选）
 * @param openid            用户 openid（可选）
 * @param packageInfo       package（可选）
 * @param eventType         事件类型（可选）
 */
public record WechatPayScoreParseResult(
        String outOrderNo,
        String channelOrderId,
        String state,
        String stateDescription,
        Long totalAmountCents,
        String openid,
        String packageInfo,
        String eventType) {
}
