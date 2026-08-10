package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 微信商家转账申请命令。
 *
 * @param appId           商户 AppId
 * @param outBillNo       商户转账单号
 * @param transferSceneId 转账场景 ID
 * @param openid          收款用户 openid
 * @param userName        收款用户姓名（单笔≥2000 元时渠道要求；本系统单笔上限 200 元通常可空）
 * @param transferAmount  转账金额（分）
 * @param transferRemark  转账备注
 * @param notifyUrl       转账结果回调 URL
 */
public record WechatTransferCommand(
        String appId,
        String outBillNo,
        String transferSceneId,
        String openid,
        String userName,
        long transferAmount,
        String transferRemark,
        String notifyUrl) {
}
