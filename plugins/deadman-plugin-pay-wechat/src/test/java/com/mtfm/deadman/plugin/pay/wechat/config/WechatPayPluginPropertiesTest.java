package com.mtfm.deadman.plugin.pay.wechat.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * 微信支付凭证校验与双账户解析单元测试。
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

    @Test
    void resolveOrdinaryShouldPreferNestedAccountOverRoot() {
        WechatPayPluginProperties properties = completeCredentials();
        properties.setOrdinary(account("ordinary-mch", "/tmp/ordinary.pem"));
        assertThat(properties.resolveOrdinaryAccount().getMchId()).isEqualTo("ordinary-mch");
        assertThat(properties.getMchId()).isEqualTo("ordinary-mch");
        assertThat(properties.resolveOrdinaryAccount().getPrivateKeyPath()).isEqualTo("/tmp/ordinary.pem");
    }

    @Test
    void resolvePartnerShouldUseNestedAccountIndependently() {
        WechatPayPluginProperties properties = completeCredentials();
        properties.setPartner(account("partner-mch", "/tmp/partner.pem"));
        assertThat(properties.resolveOrdinaryAccount().getMchId()).isEqualTo("1747659468");
        assertThat(properties.resolvePartnerAccount().getMchId()).isEqualTo("partner-mch");
        assertThat(properties.resolvePartnerMchid()).isEqualTo("partner-mch");
        assertThat(properties.resolvePartnerAccount().getPrivateKeyPath()).isEqualTo("/tmp/partner.pem");
    }

    @Test
    void resolvePartnerShouldFallbackToOrdinaryWhenUnset() {
        WechatPayPluginProperties properties = completeCredentials();
        assertThat(properties.resolvePartnerAccount().getMchId()).isEqualTo("1747659468");
        assertThat(properties.resolvePartnerMchid()).isEqualTo("1747659468");
    }

    @Test
    void resolvePartnerShouldUseRootPartnerMchidWithOrdinaryKeys() {
        WechatPayPluginProperties properties = completeCredentials();
        properties.setPartnerMchid("1750034567");
        WechatPayAccountProperties partner = properties.resolvePartnerAccount();
        assertThat(partner.getMchId()).isEqualTo("1750034567");
        assertThat(partner.getPrivateKeyPath()).isEqualTo("/tmp/apiclient_key.pem");
        assertThat(properties.resolvePartnerMchid()).isEqualTo("1750034567");
    }

    @Test
    void requireShouldFailWhenPartnerPartial() {
        WechatPayPluginProperties properties = completeCredentials();
        WechatPayAccountProperties partner = new WechatPayAccountProperties();
        partner.setMchId("1750034567");
        properties.setPartner(partner);
        assertThatThrownBy(properties::requireRealGatewayCredentials)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("partner")
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

    private static WechatPayAccountProperties account(String mchId, String privateKeyPath) {
        WechatPayAccountProperties account = new WechatPayAccountProperties();
        account.setMchId(mchId);
        account.setApiV3Key("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
        account.setMerchantSerialNo("SERIAL" + mchId);
        account.setPrivateKeyPath(privateKeyPath);
        return account;
    }
}
