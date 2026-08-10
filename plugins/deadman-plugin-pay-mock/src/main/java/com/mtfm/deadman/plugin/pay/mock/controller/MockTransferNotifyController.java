package com.mtfm.deadman.plugin.pay.mock.controller;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mtfm.deadman.plugin.pay.mock.constant.MockPayProviderIds;
import com.mtfm.deadman.plugin.pay.mock.provider.MockTransferProvider;
import com.mtfm.deadman.plugin.pay.service.TransferService;
import com.mtfm.deadman.plugin.pay.spi.common.ChannelNotifyContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Mock 商家转账结果回调 Controller。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.pay-mock", name = "enabled", havingValue = "true")
public class MockTransferNotifyController {

    private final TransferService transferService;

    /**
     * 接收 Mock 商家转账结果回调。
     * <p>
     * 请求体示例：{@code {"out_bill_no":"TF...","status":"SUCCESS","transfer_amount":100}}
     *
     * @param body 回调请求体原文
     * @return 简易应答
     */
    @PostMapping("${deadman.plugin.pay-mock.transfer-notify-endpoint:/client/api/pay/mock/transfer/notify}")
    public Map<String, String> notify(@RequestBody String body) {
        try {
            transferService.handleTransferNotify(MockTransferProvider.PROVIDER_ID, new ChannelNotifyContext(body));
            return Map.of("code", "SUCCESS", "message", "ok");
        } catch (Exception ex) {
            log.warn("Mock 商家转账回调处理失败：provider={}", MockPayProviderIds.MOCK, ex);
            return Map.of("code", "FAIL", "message", ex.getMessage() == null ? "error" : ex.getMessage());
        }
    }
}
