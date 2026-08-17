package com.mtfm.deadman.plugin.pay.facade;

import org.springframework.stereotype.Service;

import com.mtfm.deadman.plugin.pay.manager.PayScoreProviderManager;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScoreCancelContext;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScoreCompleteContext;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScoreCompleteResult;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScoreCreateContext;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScoreCreateResult;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScoreNotifyResult;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScorePermissionContext;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScorePermissionResult;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScoreProvider;
import com.mtfm.deadman.plugin.pay.spi.payscore.PayScoreQueryResult;

import lombok.RequiredArgsConstructor;

/**
 * 支付分薄门面：仅按 Provider 转发渠道调用，不落库、不做业务编排。
 */
@Service
@RequiredArgsConstructor
public class PayScoreFacade {

    private final PayScoreProviderManager payScoreProviderManager;

    /**
     * 创建支付分服务订单。
     *
     * @param providerId 提供商标识，空则使用默认
     * @param context    创建上下文
     * @param outOrderNo 商户服务订单号
     * @return 创建结果
     */
    public PayScoreCreateResult createServiceOrder(
            String providerId, PayScoreCreateContext context, String outOrderNo) {
        return require(providerId).createServiceOrder(context, outOrderNo);
    }

    /**
     * 查询支付分服务订单。
     *
     * @param providerId 提供商标识
     * @param outOrderNo 商户服务订单号
     * @param serviceId  服务 ID
     * @return 查询结果
     */
    public PayScoreQueryResult queryServiceOrder(String providerId, String outOrderNo, String serviceId) {
        return require(providerId).queryServiceOrder(outOrderNo, serviceId);
    }

    /**
     * 取消支付分服务订单。
     *
     * @param providerId 提供商标识
     * @param context    取消上下文
     * @param outOrderNo 商户服务订单号
     */
    public void cancelServiceOrder(String providerId, PayScoreCancelContext context, String outOrderNo) {
        require(providerId).cancelServiceOrder(context, outOrderNo);
    }

    /**
     * 完结支付分服务订单。
     *
     * @param providerId 提供商标识
     * @param context    完结上下文
     * @param outOrderNo 商户服务订单号
     * @return 完结结果
     */
    public PayScoreCompleteResult completeServiceOrder(
            String providerId, PayScoreCompleteContext context, String outOrderNo) {
        return require(providerId).completeServiceOrder(context, outOrderNo);
    }

    /**
     * 创建支付分授权。
     *
     * @param providerId 提供商标识
     * @param context    授权上下文
     * @return 授权结果
     */
    public PayScorePermissionResult createPermission(String providerId, PayScorePermissionContext context) {
        return require(providerId).createPermission(context);
    }

    /**
     * 按 openid 查询支付分授权。
     *
     * @param providerId 提供商标识
     * @param openid     用户 openid
     * @param serviceId  服务 ID
     * @return 授权结果
     */
    public PayScorePermissionResult queryPermissionByOpenid(
            String providerId, String openid, String serviceId) {
        return require(providerId).queryPermissionByOpenid(openid, serviceId);
    }

    /**
     * 按 openid 解除支付分授权。
     *
     * @param providerId 提供商标识
     * @param openid     用户 openid
     * @param serviceId  服务 ID
     * @param reason     解除原因
     */
    public void terminatePermissionByOpenid(
            String providerId, String openid, String serviceId, String reason) {
        require(providerId).terminatePermissionByOpenid(openid, serviceId, reason);
    }

    /**
     * 解析支付分回调。
     *
     * @param providerId 提供商标识
     * @param context    回调上下文
     * @return 解析结果
     */
    public PayScoreNotifyResult parseNotify(String providerId, ChannelNotifyContext context) {
        return require(providerId).parseNotify(context);
    }

    private PayScoreProvider require(String providerId) {
        return payScoreProviderManager.require(providerId);
    }
}
