package com.mtfm.deadman.plugin.pay.wechat.vo;

/**
 * 收付通合单子单命令。
 *
 * @param subMchid      二级商户号（写入 sub_orders.sub_mchid；mchid 由合单发起方商户号填充）
 * @param outTradeNo    子单商户订单号
 * @param description   商品描述
 * @param amountTotal   子单金额（分）
 * @param attach        附加数据（渠道必填；空时网关写空串）
 * @param profitSharing 是否分账
 */
public record WechatEcommerceCombineSubOrderCommand(
        String subMchid,
        String outTradeNo,
        String description,
        int amountTotal,
        String attach,
        boolean profitSharing) {
}
