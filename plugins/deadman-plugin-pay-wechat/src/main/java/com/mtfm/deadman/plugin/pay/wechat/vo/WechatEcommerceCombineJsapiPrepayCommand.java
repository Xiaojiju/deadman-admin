package com.mtfm.deadman.plugin.pay.wechat.vo;

import java.util.List;

/**
 * 收付通合单 JSAPI 预下单命令。
 *
 * @param combineAppid      合单 AppId
 * @param combineMchid      合单发起方商户号（平台商户号）
 * @param combineOutTradeNo 合单商户订单号
 * @param openid            付款人 openid
 * @param notifyUrl         支付结果回调 URL
 * @param subOrders         子单列表
 * @param payerClientIp     付款人客户端 IP（可选）
 */
public record WechatEcommerceCombineJsapiPrepayCommand(
        String combineAppid,
        String combineMchid,
        String combineOutTradeNo,
        String openid,
        String notifyUrl,
        List<WechatEcommerceCombineSubOrderCommand> subOrders,
        String payerClientIp) {
}
