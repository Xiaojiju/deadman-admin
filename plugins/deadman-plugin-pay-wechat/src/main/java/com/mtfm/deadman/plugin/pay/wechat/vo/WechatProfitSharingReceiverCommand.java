package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 收付通分账接收方命令。
 *
 * @param type        接收方类型（如 MERCHANT_ID / PERSONAL_OPENID）
 * @param account     接收方账号
 * @param amount      分账金额（分）
 * @param description 分账描述
 */
public record WechatProfitSharingReceiverCommand(
        String type, String account, int amount, String description) {
}
