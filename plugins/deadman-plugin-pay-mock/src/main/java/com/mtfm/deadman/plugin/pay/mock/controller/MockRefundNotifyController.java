package com.mtfm.deadman.plugin.pay.mock.controller;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mtfm.deadman.plugin.pay.mock.constant.MockPayProviderIds;
import com.mtfm.deadman.plugin.pay.mock.provider.MockRefundProvider;
import com.mtfm.deadman.plugin.pay.service.RefundService;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Mock 退款结果回调 Controller，用于本地联调模拟渠道退款通知。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.pay-mock", name = "enabled", havingValue = "true")
public class MockRefundNotifyController {

    private final RefundService refundService;

    /**
     * 接收 Mock 退款结果回调。
     * <p>
     * 请求体示例：
     * {@code {"out_refund_no":"RF...","out_trade_no":"PO...","status":"SUCCESS","refund":100}}
     *
     * @param body 回调请求体原文
     * @return 简易应答
     */
    @PostMapping("${deadman.plugin.pay-mock.refund-notify-endpoint:/client/api/pay/mock/refund/notify}")
    public Map<String, String> notify(@RequestBody String body) {
        try {
            refundService.handleRefundNotify(MockRefundProvider.PROVIDER_ID, new ChannelNotifyContext(body));
            return Map.of("code", "SUCCESS", "message", "ok");
        } catch (Exception ex) {
            log.warn("Mock 退款回调处理失败：provider={}", MockPayProviderIds.MOCK, ex);
            return Map.of("code", "FAIL", "message", ex.getMessage() == null ? "error" : ex.getMessage());
        }
    }
}
