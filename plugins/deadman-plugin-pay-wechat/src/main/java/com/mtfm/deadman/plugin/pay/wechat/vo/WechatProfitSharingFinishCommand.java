package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 收付通完结分账命令。
 *
 * @param subMchid      子商户号
 * @param transactionId 微信订单号
 * @param outOrderNo    商户分账单号
 * @param description   完结描述
 */
public record WechatProfitSharingFinishCommand(
        String subMchid, String transactionId, String outOrderNo, String description) {
}
