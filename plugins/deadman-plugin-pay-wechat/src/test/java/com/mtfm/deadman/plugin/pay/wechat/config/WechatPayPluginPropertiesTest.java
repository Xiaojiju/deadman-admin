package com.mtfm.deadman.plugin.pay.wechat.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * 微信支付凭证校验单元测试。
 */
class WechatPayPluginPropertiesTest {

    @Test
    void requireRealGatewayCredentialsShouldPassWhenCoreFieldsPresent() {
        WechatPayPluginProperties properties = completeCredentials();
        properties.requireRealGatewayCredentials();
        assertThat(properties.usePublicKeyVerifier()).isFalse();
    }

    @Test
    void requireRealGatewayCredentialsShouldPassWhenPublicKeyPairPresent() {
        WechatPayPluginProperties properties = completeCredentials();
        properties.setPublicKeyPath("/tmp/pub_key.pem");
        properties.setPublicKeyId("PUB_KEY_ID_TEST");
        properties.requireRealGatewayCredentials();
        assertThat(properties.usePublicKeyVerifier()).isTrue();
    }

    @Test
    void requireRealGatewayCredentialsShouldFailWhenPublicKeyPairIncomplete() {
        WechatPayPluginProperties properties = completeCredentials();
        properties.setPublicKeyPath("/tmp/pub_key.pem");
        assertThatThrownBy(properties::requireRealGatewayCredentials)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("publicKeyPath")
                .hasMessageContaining("publicKeyId");
    }

    @Test
    void requireRealGatewayCredentialsShouldFailWhenPrivateKeyMissing() {
        WechatPayPluginProperties properties = completeCredentials();
        properties.setPrivateKeyPath(" ");
        assertThatThrownBy(properties::requireRealGatewayCredentials)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("privateKeyPath");
    }

    private static WechatPayPluginProperties completeCredentials() {
        WechatPayPluginProperties properties = new WechatPayPluginProperties();
        properties.setMchId("1747659468");
        properties.setApiV3Key("qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq");
        properties.setMerchantSerialNo("6819891576BE05BEAC8F80940402C454D4AE9835");
        properties.setPrivateKeyPath("/tmp/apiclient_key.pem");
        return properties;
    }
}
