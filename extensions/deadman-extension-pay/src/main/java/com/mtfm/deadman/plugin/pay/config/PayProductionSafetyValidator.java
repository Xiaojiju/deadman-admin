package com.mtfm.deadman.plugin.pay.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 生产安全校验：禁止在 prod/production 配置下启用 Mock 支付、微信 Mock 网关或支付/进件 test-mode。
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "deadman.plugin.pay", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PayProductionSafetyValidator implements ApplicationRunner {

    private final Environment environment;
    private final PayPluginProperties payPluginProperties;

    /**
     * 启动时校验危险开关。
     *
     * @param args 启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        if (!isProductionProfile()) {
            return;
        }
        if (payPluginProperties.isPaymentTestModeEnabled()) {
            throw new IllegalStateException("生产环境禁止开启 deadman.plugin.pay.test-mode.payment.enabled");
        }
        if (payPluginProperties.isSubMerchantTestModeEnabled()) {
            throw new IllegalStateException("生产环境禁止开启 deadman.plugin.pay.test-mode.sub-merchant.enabled");
        }
        String defaultProvider = payPluginProperties.getDefaultProvider();
        if (StringUtils.hasText(defaultProvider) && "mock".equalsIgnoreCase(defaultProvider.trim())) {
            throw new IllegalStateException("生产环境禁止将 deadman.plugin.pay.default-provider 设为 mock");
        }
        boolean mockPayEnabled = environment.getProperty("deadman.plugin.pay-mock.enabled", Boolean.class, false);
        if (Boolean.TRUE.equals(mockPayEnabled)) {
            throw new IllegalStateException("生产环境禁止开启 deadman.plugin.pay-mock.enabled");
        }
        boolean wechatMock =
                environment.getProperty("deadman.plugin.pay-wechat.mock-enabled", Boolean.class, false);
        if (Boolean.TRUE.equals(wechatMock)) {
            throw new IllegalStateException("生产环境禁止开启 deadman.plugin.pay-wechat.mock-enabled");
        }
        log.info("支付生产安全校验通过：已禁止 Mock/test-mode");
    }

    private boolean isProductionProfile() {
        for (String profile : environment.getActiveProfiles()) {
            if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }
}
