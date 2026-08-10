package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 微信异常退款命令。
 *
 * @param channelRefundId 微信退款单号 refund_id
 * @param outRefundNo 商户退款单号
 * @param receiveType 入账方式 USER_BANK_CARD / MERCHANT_BANK_CARD
 * @param bankType 开户银行（退至用户时必填）
 * @param bankAccount 银行卡号明文（退至用户时必填）
 * @param realName 收款人姓名明文（退至用户时必填）
 */
public record WechatAbnormalRefundCommand(
        String channelRefundId,
        String outRefundNo,
        String receiveType,
        String bankType,
        String bankAccount,
        String realName) {
}
