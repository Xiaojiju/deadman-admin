package com.mtfm.deadman.plugin.pay.alipay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 支付宝支付插件配置。
 * <p>
 * 支付宝无微信式原生「基本账户 / 运营账户」物理隔离，必须通过
 * {@code DirectPayFacade} / {@code EcommerceTradeFacade} / {@code PayoutFacade}
 * 与 {@code PlatformFundLedgerService} 做业务级双账户隔离。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.pay-alipay")
public class AlipayPayPluginProperties {

    /** 是否启用支付宝插件（骨架阶段默认 false） */
    private boolean enabled = false;

    /** 应用 AppId（正式接入时填写） */
    private String appId;

    /** 是否使用 Mock（正式渠道 SDK 接入前可保持 true 做联调占位） */
    private boolean mockEnabled = true;
}
