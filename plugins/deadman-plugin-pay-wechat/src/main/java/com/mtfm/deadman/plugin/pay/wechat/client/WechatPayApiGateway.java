package com.mtfm.deadman.plugin.pay.wechat.client;

import com.mtfm.deadman.plugin.pay.spi.PaymentNotifyContext;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatJsapiPrepayCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayNotifyParseResult;

/**
 * 微信支付 API 网关，封装统一下单、签名与回调解析。
 */
public interface WechatPayApiGateway {

    /**
     * 创建 JSAPI 预下单并生成小程序调起支付参数。
     *
     * @param command 预下单命令（含 Provider 独立 AppId 与回调 URL）
     * @return 预下单结果
     */
    WechatPayJsapiPrepayResult createJsapiPrepay(WechatJsapiPrepayCommand command);

    /**
     * 解析微信支付结果回调（验签 + 解密）。
     *
     * @param context 回调上下文
     * @return 解析结果
     */
    WechatPayNotifyParseResult parseNotify(PaymentNotifyContext context);

    /**
     * 按商户订单号查询微信支付单状态。
     *
     * @param outTradeNo 平台支付单号
     * @return 查单结果
     */
    WechatPayNotifyParseResult queryOrderByOutTradeNo(String outTradeNo);
}
