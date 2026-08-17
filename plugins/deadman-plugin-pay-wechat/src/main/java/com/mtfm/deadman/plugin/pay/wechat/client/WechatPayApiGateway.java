package com.mtfm.deadman.plugin.pay.wechat.client;

import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.wechat.client.model.PayScorePermissionResponse;
import com.mtfm.deadman.plugin.pay.wechat.client.model.PayScoreServiceOrderResponse;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatAbnormalRefundCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatAddProfitSharingReceiverCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceApplymentCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceApplymentResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceCombineJsapiPrepayCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceCombinePrepayResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceRefundCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatJsapiPrepayCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatMediaUploadResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayNotifyParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScoreCancelCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScoreCompleteCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScoreCreateCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScoreParseResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatPayScorePermissionCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingCreateCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingCreateResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingFinishCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingQueryResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingReturnCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatProfitSharingReturnResult;
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
     * 按商户退款单号查询收付通退款（须带二级商户号）。
     *
     * @param outRefundNo 平台退款单号
     * @param subMchid    二级商户号
     * @return 查退款结果
     */
    WechatRefundParseResult queryEcommerceRefundByOutRefundNo(String outRefundNo, String subMchid);

    /**
     * 解析收付通退款结果回调（验签 + 解密，资源模型含 sub_mchid / refund_status）。
     *
     * @param context 回调上下文
     * @return 解析结果
     */
    WechatRefundParseResult parseEcommerceRefundNotify(ChannelNotifyContext context);

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

    /**
     * 创建收付通合单 JSAPI 预下单并生成调起支付参数。
     *
     * @param command 合单预下单命令
     * @return 预下单结果
     */
    WechatEcommerceCombinePrepayResult createEcommerceCombineJsapiPrepay(
            WechatEcommerceCombineJsapiPrepayCommand command);

    /**
     * 按合单商户订单号查询收付通合单状态。
     *
     * @param combineOutTradeNo 合单商户订单号
     * @return 查单结果（合单号映射为 outTradeNo）
     */
    WechatPayNotifyParseResult queryEcommerceCombineOrder(String combineOutTradeNo);

    /**
     * 解析收付通合单支付结果回调（验签 + 解密；资源结构与直连 Transaction 不同）。
     *
     * @param context 回调上下文
     * @return 解析结果
     */
    WechatPayNotifyParseResult parseEcommerceCombineNotify(ChannelNotifyContext context);

    /**
     * 发起收付通请求分账。
     *
     * @param command 分账命令
     * @return 分账结果
     */
    WechatProfitSharingCreateResult createEcommerceProfitSharing(WechatProfitSharingCreateCommand command);

    /**
     * 查询收付通分账单。
     *
     * @param subMchid      子商户号
     * @param transactionId 微信订单号
     * @param outOrderNo    商户分账单号
     * @return 查询结果
     */
    WechatProfitSharingQueryResult queryEcommerceProfitSharing(
            String subMchid, String transactionId, String outOrderNo);

    /**
     * 完结收付通分账。
     *
     * @param command 完结分账命令
     * @return 分账结果（字段与创建分账结果对齐）
     */
    WechatProfitSharingCreateResult finishEcommerceProfitSharing(WechatProfitSharingFinishCommand command);

    /**
     * 发起收付通分账回退。
     *
     * @param command 回退命令
     * @return 回退结果
     */
    WechatProfitSharingReturnResult returnEcommerceProfitSharing(WechatProfitSharingReturnCommand command);

    /**
     * 添加收付通分账接收方。
     *
     * @param command 添加接收方命令
     */
    void addEcommerceProfitSharingReceiver(WechatAddProfitSharingReceiverCommand command);

    /**
     * 提交收付通二级商户进件（结构化命令）。
     *
     * @param command 进件命令
     * @return 进件结果
     */
    WechatEcommerceApplymentResult createEcommerceApplyment(WechatEcommerceApplymentCommand command);

    /**
     * 按业务申请编号查询收付通进件状态。
     *
     * @param outRequestNo 业务申请编号
     * @return 进件结果
     */
    WechatEcommerceApplymentResult queryEcommerceApplyment(String outRequestNo);

    /**
     * 上传媒体文件到微信（进件证件/执照/店铺二维码等）。
     * <p>
     * 对应 {@code POST /v3/merchant/media/upload}，支持 JPG/BMP/PNG，不超过 5MB。
     *
     * @param fileName 文件名（须含合法后缀）
     * @param content  文件二进制内容
     * @return 含 MediaID 的上传结果
     * @see <a href="https://pay.weixin.qq.com/doc/v3/partner/4012760432">文件上传</a>
     */
    WechatMediaUploadResult uploadMedia(String fileName, byte[] content);

    /**
     * 发起收付通退款申请。
     *
     * @param command 退款命令
     * @return 退款结果
     */
    WechatRefundParseResult createEcommerceRefund(WechatEcommerceRefundCommand command);

    /**
     * 创建支付分服务订单。
     *
     * @param cmd 创建命令
     * @return 服务订单响应
     */
    PayScoreServiceOrderResponse createPayScoreServiceOrder(WechatPayScoreCreateCommand cmd);

    /**
     * 查询支付分服务订单。
     *
     * @param appId      商户 AppId
     * @param serviceId  服务 ID
     * @param outOrderNo 商户服务订单号
     * @return 服务订单响应
     */
    PayScoreServiceOrderResponse queryPayScoreServiceOrder(String appId, String serviceId, String outOrderNo);

    /**
     * 取消支付分服务订单。
     *
     * @param cmd 取消命令
     * @return 服务订单响应
     */
    PayScoreServiceOrderResponse cancelPayScoreServiceOrder(WechatPayScoreCancelCommand cmd);

    /**
     * 完结支付分服务订单。
     *
     * @param cmd 完结命令
     * @return 服务订单响应
     */
    PayScoreServiceOrderResponse completePayScoreServiceOrder(WechatPayScoreCompleteCommand cmd);

    /**
     * 创建支付分授权（预授权）。
     *
     * @param cmd 授权命令
     * @return 授权响应
     */
    PayScorePermissionResponse createPayScorePermission(WechatPayScorePermissionCommand cmd);

    /**
     * 按 openid 查询支付分授权。
     *
     * @param appId     商户 AppId
     * @param serviceId 服务 ID
     * @param openid    用户 openid
     * @return 授权响应
     */
    PayScorePermissionResponse queryPayScorePermissionByOpenid(String appId, String serviceId, String openid);

    /**
     * 按 openid 解除支付分授权。
     *
     * @param appId     商户 AppId
     * @param serviceId 服务 ID
     * @param openid    用户 openid
     * @param reason    解除原因
     */
    void terminatePayScorePermissionByOpenid(String appId, String serviceId, String openid, String reason);

    /**
     * 解析支付分结果回调（验签 + 解密）。
     *
     * @param context 回调上下文
     * @return 解析结果
     */
    WechatPayScoreParseResult parsePayScoreNotify(ChannelNotifyContext context);
}
