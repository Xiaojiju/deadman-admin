package com.mtfm.deadman.plugin.pay.wechat.vo;

import java.util.List;

import com.mtfm.deadman.plugin.pay.spi.payscore.PayScorePostPayment;

/**
 * 微信支付分完结服务订单命令。
 *
 * @param appId             商户 AppId
 * @param serviceId         服务 ID
 * @param outOrderNo        商户服务订单号
 * @param postPayments      后付费项目
 * @param totalAmountCents  总金额（分）
 * @param postDiscounts     优惠项目（可选）
 * @param timeRangeEndTime  服务结束时间（可选）
 * @param completeTime      完结时间（可选）
 */
public record WechatPayScoreCompleteCommand(
        String appId,
        String serviceId,
        String outOrderNo,
        List<PayScorePostPayment> postPayments,
        long totalAmountCents,
        List<PayScorePostPayment> postDiscounts,
        String timeRangeEndTime,
        String completeTime) {
}
