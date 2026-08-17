package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 收付通分账回退结果。
 *
 * @param subMchid    子商户号
 * @param orderId     微信分账单号
 * @param outOrderNo  商户分账单号
 * @param outReturnNo 商户回退单号
 * @param returnMchid 回退商户号
 * @param amount      回退金额（分）
 * @param result      回退结果状态
 */
public record WechatProfitSharingReturnResult(
        String subMchid,
        String orderId,
        String outOrderNo,
        String outReturnNo,
        String returnMchid,
        Integer amount,
        String result) {
}
