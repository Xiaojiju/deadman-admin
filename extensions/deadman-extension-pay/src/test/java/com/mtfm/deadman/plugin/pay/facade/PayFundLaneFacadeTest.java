package com.mtfm.deadman.plugin.pay.facade;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.plugin.pay.service.PayService;
import com.mtfm.deadman.plugin.pay.service.PaymentOrderService;
import com.mtfm.deadman.plugin.pay.service.ProfitSharingService;
import com.mtfm.deadman.plugin.pay.service.RefundService;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentCombineSubOrder;
import com.mtfm.deadman.plugin.pay.spi.payment.PaymentPrepayContext;

/**
 * 三分域门面资金链路校验单测。
 */
class PayFundLaneFacadeTest {

    @Test
    void directFacadeRejectsCombine() {
        PayService payService = mock(PayService.class);
        DirectPayFacade facade = new DirectPayFacade(payService, mock(RefundService.class), mock(PaymentOrderService.class));
        PaymentPrepayContext context = PaymentPrepayContext.builder()
                .bizOrderNo("B1")
                .description("x")
                .amountTotal(100)
                .subOrders(List.of(new PaymentCombineSubOrder("sub", "s1", "d", 100, true)))
                .build();
        assertThrows(BusinessException.class, () -> facade.createPrepay(context, "wechat-jsapi"));
        verifyNoInteractions(payService);
    }

    @Test
    void ecommerceFacadeRejectsNonCombine() {
        PayService payService = mock(PayService.class);
        EcommerceTradeFacade facade = new EcommerceTradeFacade(
                payService,
                mock(RefundService.class),
                mock(PaymentOrderService.class),
                mock(ProfitSharingService.class));
        PaymentPrepayContext context = PaymentPrepayContext.builder()
                .bizOrderNo("B1")
                .description("x")
                .amountTotal(100)
                .build();
        assertThrows(BusinessException.class, () -> facade.createCombinePrepay(context, "wechat-ecommerce-combine-jsapi"));
        verifyNoInteractions(payService);
    }

    @Test
    void ecommerceFacadeAcceptsCombine() {
        PayService payService = mock(PayService.class);
        EcommerceTradeFacade facade = new EcommerceTradeFacade(
                payService,
                mock(RefundService.class),
                mock(PaymentOrderService.class),
                mock(ProfitSharingService.class));
        PaymentPrepayContext context = PaymentPrepayContext.builder()
                .bizOrderNo("B1")
                .description("x")
                .amountTotal(100)
                .subOrders(List.of(new PaymentCombineSubOrder("sub", "s1", "d", 100, true)))
                .build();
        facade.createCombinePrepay(context, "wechat-ecommerce-combine-jsapi");
        verify(payService).createPrepay(context, "wechat-ecommerce-combine-jsapi", "ECOMMERCE");
    }
}
