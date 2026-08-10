package com.mtfm.deadman.plugin.pay.wechat.client;

import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatAbnormalRefundCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatJsapiPrepayCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayNotifyParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatRefundCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatRefundParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatTransferCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatTransferParseResult;

/**
 * 微信支付 API 网关，封装统一下单、退款、签名与回调解析。
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
    WechatPayNotifyParseResult parseNotify(ChannelNotifyContext context);

    /**
     * 按商户订单号查询微信支付单状态。
     *
     * @param outTradeNo 平台支付单号
     * @return 查单结果
     */
    WechatPayNotifyParseResult queryOrderByOutTradeNo(String outTradeNo);

    /**
     * 发起微信退款申请。
     *
     * @param command 退款命令
     * @return 退款结果
     */
    WechatRefundParseResult createRefund(WechatRefundCommand command);

    /**
     * 解析微信退款结果回调（验签 + 解密）。
     *
     * @param context 回调上下文
     * @return 解析结果
     */
    WechatRefundParseResult parseRefundNotify(ChannelNotifyContext context);

    /**
     * 按商户退款单号查询退款状态。
     *
     * @param outRefundNo 平台退款单号
     * @return 查退款结果
     */
    WechatRefundParseResult queryRefundByOutRefundNo(String outRefundNo);

    /**
     * 发起微信异常退款。
     *
     * @param command 异常退款命令
     * @return 退款结果
     */
    WechatRefundParseResult createAbnormalRefund(WechatAbnormalRefundCommand command);

    /**
     * 发起微信商家转账。
     *
     * @param command 转账命令
     * @return 转账结果
     */
    WechatTransferParseResult createTransfer(WechatTransferCommand command);

    /**
     * 解析微信商家转账结果回调。
     *
     * @param context 回调上下文
     * @return 解析结果
     */
    WechatTransferParseResult parseTransferNotify(ChannelNotifyContext context);

    /**
     * 按商户转账单号查询转账状态。
     *
     * @param outBillNo 商户转账单号
     * @return 查单结果
     */
    WechatTransferParseResult queryTransferByOutBillNo(String outBillNo);
}
