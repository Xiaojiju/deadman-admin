package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 收付通分账回退命令。
 *
 * @param subMchid    子商户号
 * @param orderId     微信分账单号（与 outOrderNo 二选一）
 * @param outOrderNo  商户分账单号（与 orderId 二选一）
 * @param outReturnNo 商户回退单号
 * @param returnMchid 回退商户号
 * @param amount      回退金额（分）
 * @param description 回退描述
 */
public record WechatProfitSharingReturnCommand(
        String subMchid,
        String orderId,
        String outOrderNo,
        String outReturnNo,
        String returnMchid,
        int amount,
        String description) {
}
