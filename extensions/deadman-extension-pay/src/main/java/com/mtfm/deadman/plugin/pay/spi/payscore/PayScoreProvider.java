package com.mtfm.deadman.plugin.pay.spi.payscore;

import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;

/**
 * 支付分 Provider SPI（创建/查询/取消/完结服务订单，授权与回调解析）。
 */
public interface PayScoreProvider {

    /**
     * 提供商标识，如 {@code wechat-payscore}。
     *
     * @return 提供商标识
     */
    String providerId();

    /**
     * 支付平台标识，如 {@code WECHAT}。
     *
     * @return 支付平台
     */
    String payPlatform();

    /**
     * 是否支持指定标识。
     *
     * @param providerId 提供商标识
     * @return 是否支持
     */
    default boolean supports(String providerId) {
        return providerId().equals(providerId);
    }

    /**
     * 创建支付分服务订单。
     *
     * @param context    创建上下文
     * @param outOrderNo 商户服务订单号
     * @return 创建结果
     */
    PayScoreCreateResult createServiceOrder(PayScoreCreateContext context, String outOrderNo);

    /**
     * 查询支付分服务订单。
     *
     * @param outOrderNo 商户服务订单号
     * @param serviceId  服务 ID
     * @return 查询结果
     */
    PayScoreQueryResult queryServiceOrder(String outOrderNo, String serviceId);

    /**
     * 取消支付分服务订单。
     *
     * @param context    取消上下文
     * @param outOrderNo 商户服务订单号
     */
    void cancelServiceOrder(PayScoreCancelContext context, String outOrderNo);

    /**
     * 完结支付分服务订单。
     *
     * @param context    完结上下文
     * @param outOrderNo 商户服务订单号
     * @return 完结结果
     */
    PayScoreCompleteResult completeServiceOrder(PayScoreCompleteContext context, String outOrderNo);

    /**
     * 创建支付分授权（预授权）。
     *
     * @param context 授权上下文
     * @return 授权结果
     */
    PayScorePermissionResult createPermission(PayScorePermissionContext context);

    /**
     * 按 openid 查询支付分授权状态。
     *
     * @param openid    用户 openid
     * @param serviceId 服务 ID
     * @return 授权结果
     */
    PayScorePermissionResult queryPermissionByOpenid(String openid, String serviceId);

    /**
     * 按 openid 解除支付分授权。
     *
     * @param openid    用户 openid
     * @param serviceId 服务 ID
     * @param reason    解除原因
     */
    void terminatePermissionByOpenid(String openid, String serviceId, String reason);

    /**
     * 解析支付分回调。
     *
     * @param context 回调上下文
     * @return 解析结果
     */
    PayScoreNotifyResult parseNotify(ChannelNotifyContext context);
}
