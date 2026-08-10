package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 微信商家转账解析结果（申请/查单/回调共用）。
 *
 * @param outBillNo     商户转账单号
 * @param channelBillNo 微信转账单号
 * @param amountCents   金额（分，可选）
 * @param state         微信 state
 * @param packageInfo   用户确认 package
 * @param failReason    失败原因
 */
public record WechatTransferParseResult(
        String outBillNo,
        String channelBillNo,
        Long amountCents,
        String state,
        String packageInfo,
        String failReason) {
}
