package com.mtfm.deadman.plugin.pay.wechat.vo;

import java.util.List;

/**
 * 收付通请求分账命令。
 *
 * @param subMchid      子商户号
 * @param transactionId 微信订单号
 * @param outOrderNo    商户分账单号
 * @param receivers     分账接收方列表
 * @param finish        是否完结分账
 */
public record WechatProfitSharingCreateCommand(
        String subMchid,
        String transactionId,
        String outOrderNo,
        List<WechatProfitSharingReceiverCommand> receivers,
        boolean finish) {
}
