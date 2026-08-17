package com.mtfm.deadman.plugin.pay.facade;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PayFundLane;
import com.mtfm.deadman.plugin.pay.entity.PaymentOrder;
import com.mtfm.deadman.plugin.pay.service.PayService;
import com.mtfm.deadman.plugin.pay.service.PaymentOrderService;
import com.mtfm.deadman.plugin.pay.service.RefundService;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentOrderSnapshot;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentPrepayContext;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentPrepayResult;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentProvider;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundContext;
import com.mtfm.deadman.plugin.pay.spi.refund.RefundOrderSnapshot;

import lombok.RequiredArgsConstructor;

/**
 * 直连支付门面：平台自营等普通支付，资金链路固定为 {@link PayFundLane#DIRECT}。
 * <p>
 * 禁止合单/二级商户子单；记账归属基本账户经营性收款，禁止从本链路直接商家转账到零钱。
 */
@Service
@RequiredArgsConstructor
public class DirectPayFacade {

    private final PayService payService;
    private final RefundService refundService;
    private final PaymentOrderService paymentOrderService;

    /**
     * 直连预下单。
     *
     * @param context    预下单上下文（不得含合单子单）
     * @param providerId 支付 Provider，空则默认
     * @return 预下单结果
     */
    public PaymentPrepayResult createPrepay(PaymentPrepayContext context, String providerId) {
        assertDirectContext(context);
        return payService.createPrepay(context, providerId, PayFundLane.DIRECT);
    }

    /**
     * 直连退款（校验支付单资金链路为 DIRECT）。
     *
     * @param request 退款请求
     * @return 退款快照
     */
    public RefundOrderSnapshot createRefund(RefundContext request) {
        if (request == null || !StringUtils.hasText(request.getOutTradeNo())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "退款缺少平台支付单号");
        }
        assertOrderLane(request.getOutTradeNo().trim(), PayFundLane.DIRECT);
        return refundService.createRefund(request);
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
        assertOrderLane(outTradeNo, PayFundLane.DIRECT);
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

    private static void assertDirectContext(PaymentPrepayContext context) {
        if (context == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "预下单上下文不能为空");
        }
        if (context.isCombinePay()) {
            throw new BusinessException(
                    ResultCode.PAY_FUND_LANE_MISMATCH, "直连支付门面禁止合单，请使用 EcommerceTradeFacade");
        }
    }

    private void assertOrderLane(String outTradeNo, String expectedLane) {
        PaymentOrder order = paymentOrderService.requireByOutTradeNo(outTradeNo);
        String lane = StringUtils.hasText(order.getFundLane()) ? order.getFundLane() : PayFundLane.DIRECT;
        if (!expectedLane.equals(lane)) {
            throw new BusinessException(
                    ResultCode.PAY_FUND_LANE_MISMATCH,
                    "支付单资金链路不匹配：actual=" + lane + ", expected=" + expectedLane);
        }
    }
}
