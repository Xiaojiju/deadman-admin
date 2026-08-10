package com.mtfm.deadman.plugin.pay.mock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Mock 支付插件配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.pay-mock")
public class MockPayPluginProperties {

    /** 是否启用 Mock 支付插件 */
    private boolean enabled = false;

    /** 本应用接收 Mock 支付回调的路径 */
    private String notifyEndpoint = "/client/api/pay/mock/notify";

    /** 本应用接收 Mock 退款回调的路径 */
    private String refundNotifyEndpoint = "/client/api/pay/mock/refund/notify";

    /** 本应用接收 Mock 商家转账回调的路径 */
    private String transferNotifyEndpoint = "/client/api/pay/mock/transfer/notify";

    /**
     * 预下单后是否自动完成支付（查单成功 → 发布支付成功事件）。
     * <p>
     * 测试环境通常无真实回调，默认开启；若需手动模拟回调/查单可设为 false。
     */
    private boolean autoCompleteOnPrepay = true;

    /**
     * 发起退款后是否自动按查退款结果完成。
     * <p>
     * 测试环境默认开启；若需手动模拟退款回调可设为 false。
     */
    private boolean autoCompleteOnRefund = true;

    /**
     * 发起商家转账后是否自动按查单结果完成。
     * <p>
     * 测试环境默认开启；若需手动模拟转账回调可设为 false。
     */
    private boolean autoCompleteOnTransfer = true;
}
