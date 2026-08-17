package com.mtfm.deadman.plugin.pay.facade;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PayFundLane;
import com.mtfm.deadman.plugin.pay.entity.PaymentOrder;
import com.mtfm.deadman.plugin.pay.service.PayService;
import com.mtfm.deadman.plugin.pay.service.PaymentOrderService;
import com.mtfm.deadman.plugin.pay.service.ProfitSharingService;
import com.mtfm.deadman.plugin.pay.service.RefundService;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentOrderSnapshot;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentPrepayContext;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentPrepayResult;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentProvider;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingCreateRequest;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingCreateResult;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingFinishRequest;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingQueryResult;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingReturnRequest;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingReturnResult;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundContext;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundOrderSnapshot;

import lombok.RequiredArgsConstructor;

/**
 * 收付通交易门面：合单支付、分账、电商退款，资金链路固定为 {@link PayFundLane#ECOMMERCE}。
 * <p>
 * 分账入账服务商基本账户（BASIC）；禁止使用运营账户参与分账或交易结算。
 */
@Service
@RequiredArgsConstructor
public class EcommerceTradeFacade {

    private final PayService payService;
    private final RefundService refundService;
    private final PaymentOrderService paymentOrderService;
    private final ProfitSharingService profitSharingService;

    /**
     * 收付通合单预下单。
     *
     * @param context    必须包含合单子单
     * @param providerId 支付 Provider（如 wechat-ecommerce-combine-jsapi）
     * @return 预下单结果
     */
    public PaymentPrepayResult createCombinePrepay(PaymentPrepayContext context, String providerId) {
        assertEcommerceContext(context);
        return payService.createPrepay(context, providerId, PayFundLane.ECOMMERCE);
    }

    /**
     * 收付通订单退款（校验资金链路为 ECOMMERCE）。
     *
     * @param request 退款请求
     * @return 退款快照
     */
    public RefundOrderSnapshot createRefund(RefundContext request) {
        if (request == null || !StringUtils.hasText(request.getOutTradeNo())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "退款缺少平台支付单号");
        }
        assertOrderLane(request.getOutTradeNo().trim(), PayFundLane.ECOMMERCE);
        return refundService.createRefund(request);
    }

    /**
     * 请求分账（接收方仅服务商商户号，入账 BASIC）。
     *
     * @param providerId       分账 Provider
     * @param request          分账请求
     * @param partnerMchid     服务商商户号
     * @param orderAmountCents 订单总额（分）
     * @return 分账结果
     */
    public ProfitSharingCreateResult createProfitSharing(
            String providerId,
            ProfitSharingCreateRequest request,
            String partnerMchid,
            int orderAmountCents) {
        return profitSharingService.create(providerId, request, partnerMchid, orderAmountCents);
    }

    /**
     * 查询分账。
     *
     * @param providerId    Provider
     * @param subMchid      二级商户号
     * @param transactionId 渠道交易号
     * @param outOrderNo    平台分账单号
     * @return 查询结果
     */
    public ProfitSharingQueryResult queryProfitSharing(
            String providerId, String subMchid, String transactionId, String outOrderNo) {
        return profitSharingService.query(providerId, subMchid, transactionId, outOrderNo);
    }

    /**
     * 完结分账（剩余货款留存二级商户）。
     *
     * @param providerId Provider
     * @param request    完结请求
     * @return 结果
     */
    public ProfitSharingQueryResult finishProfitSharing(String providerId, ProfitSharingFinishRequest request) {
        return profitSharingService.finish(providerId, request);
    }

    /**
     * 分账回退。
     *
     * @param providerId Provider
     * @param request    回退请求
     * @return 结果
     */
    public ProfitSharingReturnResult returnProfitSharing(String providerId, ProfitSharingReturnRequest request) {
        return profitSharingService.returnOrder(providerId, request);
    }

    /**
     * 添加分账接收方（仅 MERCHANT_ID）。
     *
     * @param providerId   Provider
     * @param appId        AppId
     * @param type         类型
     * @param account      商户号
     * @param relationType 关系
     */
    public void addProfitSharingReceiver(
            String providerId, String appId, String type, String account, String relationType) {
        profitSharingService.addReceiver(providerId, appId, type, account, relationType);
    }

    /**
     * 查询支付单。
     *
     * @param outTradeNo 平台支付单号
     * @return 快照
     */
    public PaymentOrderSnapshot queryOrder(String outTradeNo) {
        return payService.queryOrder(outTradeNo);
    }

    /**
     * 主动查单同步。
     *
     * @param outTradeNo 平台支付单号
     * @return 同步后快照
     */
    public PaymentOrderSnapshot syncOrderFromChannel(String outTradeNo) {
        assertOrderLane(outTradeNo, PayFundLane.ECOMMERCE);
        return payService.syncOrderFromChannel(outTradeNo);
    }

    /**
     * 按支付方式解析 Provider。
     *
     * @param paymentMethod 支付方式或 Provider 标识
     * @return Provider
     */
    public PaymentProvider requirePaymentProvider(String paymentMethod) {
        return payService.requirePaymentProvider(paymentMethod);
    }

    private static void assertEcommerceContext(PaymentPrepayContext context) {
        if (context == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "预下单上下文不能为空");
        }
        if (!context.isCombinePay()) {
            throw new BusinessException(
                    ResultCode.PAY_FUND_LANE_MISMATCH, "收付通门面必须传入合单子单，直连请用 DirectPayFacade");
        }
    }

    private void assertOrderLane(String outTradeNo, String expectedLane) {
        PaymentOrder order = paymentOrderService.requireByOutTradeNo(outTradeNo);
        String lane = order.getFundLane();
        if (!StringUtils.hasText(lane)) {
            throw new BusinessException(
                    ResultCode.PAY_FUND_LANE_MISMATCH, "支付单缺少资金链路标记，无法按收付通退款/同步");
        }
        if (!expectedLane.equals(lane)) {
            throw new BusinessException(
                    ResultCode.PAY_FUND_LANE_MISMATCH,
                    "支付单资金链路不匹配：actual=" + lane + ", expected=" + expectedLane);
        }
    }
}
