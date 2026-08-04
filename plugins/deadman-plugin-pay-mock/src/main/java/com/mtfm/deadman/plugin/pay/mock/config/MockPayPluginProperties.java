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
}
