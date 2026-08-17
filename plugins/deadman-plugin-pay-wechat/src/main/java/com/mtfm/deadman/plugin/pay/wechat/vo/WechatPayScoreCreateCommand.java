package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 微信支付分创建服务订单命令。
 *
 * @param appId                商户 AppId
 * @param serviceId            服务 ID
 * @param outOrderNo           商户服务订单号
 * @param openid               用户 openid
 * @param serviceIntroduction  服务信息
 * @param riskFundName         风险金名称
 * @param riskFundAmountCents  风险金金额（分）
 * @param timeRangeStartTime   服务开始时间（可选）
 * @param timeRangeEndTime     服务结束时间（可选）
 * @param locationName         服务位置名称（可选）
 * @param notifyUrl            回调 URL（可选）
 * @param attach               商户数据包（可选）
 * @param needUserConfirm      是否需要用户确认
 */
public record WechatPayScoreCreateCommand(
        String appId,
        String serviceId,
        String outOrderNo,
        String openid,
        String serviceIntroduction,
        String riskFundName,
        long riskFundAmountCents,
        String timeRangeStartTime,
        String timeRangeEndTime,
        String locationName,
        String notifyUrl,
        String attach,
        boolean needUserConfirm) {
}
