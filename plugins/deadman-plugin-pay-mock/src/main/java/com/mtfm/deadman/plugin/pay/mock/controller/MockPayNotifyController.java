package com.mtfm.deadman.plugin.pay.mock.controller;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mtfm.deadman.plugin.pay.mock.constant.MockPayProviderIds;
import com.mtfm.deadman.plugin.pay.mock.provider.MockPaymentProvider;
import com.mtfm.deadman.plugin.pay.service.PayService;
import com.mtfm.deadman.plugin.pay.spi.PaymentNotifyContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Mock 支付结果回调 Controller，用于本地联调模拟渠道通知。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.pay-mock", name = "enabled", havingValue = "true")
public class MockPayNotifyController {

    private final PayService payService;

    /**
     * 接收 Mock 支付结果回调。
     * <p>
     * 请求体示例：{@code {"out_trade_no":"PO...","transaction_id":"mock_tx_...","status":"SUCCESS"}}
     *
     * @param body 回调请求体原文
     * @return 简易应答
     */
    @PostMapping("${deadman.plugin.pay-mock.notify-endpoint:/client/api/pay/mock/notify}")
    public Map<String, String> notify(@RequestBody String body) {
        try {
            payService.handleNotify(MockPaymentProvider.PROVIDER_ID, new PaymentNotifyContext(body));
            return Map.of("code", "SUCCESS", "message", "ok");
        } catch (Exception ex) {
            log.warn("Mock 支付回调处理失败：provider={}", MockPayProviderIds.MOCK, ex);
            return Map.of("code", "FAIL", "message", ex.getMessage() == null ? "error" : ex.getMessage());
        }
    }
}
