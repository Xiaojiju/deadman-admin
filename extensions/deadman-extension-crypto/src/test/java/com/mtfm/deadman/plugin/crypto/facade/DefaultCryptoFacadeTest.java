package com.mtfm.deadman.plugin.crypto.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.crypto.config.CryptoPluginProperties;
import com.mtfm.deadman.plugin.crypto.constant.CryptoAlgorithms;
import com.mtfm.deadman.plugin.crypto.constant.CryptoModuleCodes;
import com.mtfm.deadman.plugin.crypto.spi.CryptoContext;
import com.mtfm.deadman.plugin.crypto.util.CryptoPayloadCodec;

/**
 * 对称信封加解密门面单测（含 AAD、严格模块密钥、轮换、工厂）。
 */
class DefaultCryptoFacadeTest {

    private CryptoFacade facade;
    private CryptoPluginProperties properties;

    @BeforeEach
    void setUp() {
        properties = baseProperties();
        facade = CryptoFacades.create(properties);
    }

    @Test
    void encryptDecryptWithDefaultKey() {
        String cipher = facade.encrypt("张三");
        assertTrue(CryptoPayloadCodec.isEncryptedToken(cipher));
        assertNotEquals("张三", cipher);
        assertEquals("张三", facade.decrypt(cipher));
    }

    @Test
    void encryptDecryptWithModuleKey() {
        String cipher = facade.encryptForModule("李四", CryptoModuleCodes.PAY);
        assertTrue(cipher.contains(".pay."));
        assertEquals("李四", facade.decrypt(cipher));
    }

    @Test
    void moduleKeyDiffersFromDefault() {
        String withPay = facade.encrypt("同一明文", CryptoContext.forModule(CryptoModuleCodes.PAY));
        String withDefault = facade.encrypt("同一明文", CryptoContext.defaults());
        assertNotEquals(withPay, withDefault);
        assertEquals("同一明文", facade.decrypt(withPay));
        assertEquals("同一明文", facade.decrypt(withDefault));
    }

    @Test
    void plaintextPassthroughOnDecrypt() {
        assertEquals("历史明文", facade.decrypt("历史明文"));
    }

    @Test
    void purposeAadPreventsCrossFieldReuse() {
        CryptoContext nameCtx = CryptoContext.forModule(CryptoModuleCodes.PAY).withPurpose("transfer.user_name");
        CryptoContext otherCtx = CryptoContext.forModule(CryptoModuleCodes.PAY).withPurpose("other.field");
        String cipher = facade.encrypt("王五", nameCtx);
        assertEquals("王五", facade.decrypt(cipher, nameCtx));
        BusinessException ex = assertThrows(BusinessException.class, () -> facade.decrypt(cipher, otherCtx));
        assertEquals(ResultCode.CRYPTO_DECRYPT_FAILED.getCode(), ex.getCode());
    }

    @Test
    void legacyCipherWithoutAadStillDecrypts() {
        String legacy = facade.encryptForModule("旧密文", CryptoModuleCodes.PAY);
        assertEquals("旧密文", facade.decrypt(legacy));
        assertEquals("旧密文", facade.decrypt(legacy, CryptoContext.forModule(CryptoModuleCodes.PAY)));
    }

    @Test
    void strictModuleKeyRejectsMissingModule() {
        CryptoPluginProperties props = baseProperties();
        props.setModuleKeys(Map.of());
        props.setStrictModuleKey(true);
        CryptoFacade local = CryptoFacades.create(props);
        BusinessException ex =
                assertThrows(BusinessException.class, () -> local.encryptForModule("x", CryptoModuleCodes.PAY));
        assertEquals(ResultCode.CRYPTO_KEY_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void additionalKeysSupportRotationDecrypt() {
        byte[] oldRaw = new byte[32];
        for (int i = 0; i < 32; i++) {
            oldRaw[i] = (byte) (100 + i);
        }
        String oldKeyB64 = Base64.getEncoder().encodeToString(oldRaw);

        CryptoPluginProperties oldProps = baseProperties();
        CryptoPluginProperties.ModuleKeyProperties payKey = new CryptoPluginProperties.ModuleKeyProperties();
        payKey.setKeyId("pay-v1");
        payKey.setKey(oldKeyB64);
        oldProps.setModuleKeys(Map.of(CryptoModuleCodes.PAY, payKey));
        CryptoFacade oldFacade = CryptoFacades.create(oldProps);
        String cipher = oldFacade.encryptForModule("轮换前", CryptoModuleCodes.PAY);
        assertTrue(cipher.contains(".pay-v1."));

        CryptoPluginProperties newProps = baseProperties();
        CryptoPluginProperties.ModuleKeyProperties newPay = new CryptoPluginProperties.ModuleKeyProperties();
        newPay.setKeyId("pay");
        newPay.setKey(properties.getModuleKeys().get(CryptoModuleCodes.PAY).getKey());
        newProps.setModuleKeys(Map.of(CryptoModuleCodes.PAY, newPay));
        newProps.setAdditionalKeys(Map.of("pay-v1", oldKeyB64));
        CryptoFacade rotated = CryptoFacades.create(newProps);
        assertEquals("轮换前", rotated.decrypt(cipher));
    }

    @Test
    void factoryRequiresKeysWhenConfigured() {
        CryptoPluginProperties empty = new CryptoPluginProperties();
        empty.setEnabled(true);
        empty.setRequireKeys(true);
        assertThrows(IllegalStateException.class, () -> CryptoFacades.create(empty));
    }

    @Test
    void disabledIsFailClosed() {
        CryptoPluginProperties props = baseProperties();
        props.setEnabled(false);
        props.setRequireKeys(false);
        CryptoFacade local = CryptoFacades.create(props);
        BusinessException ex = assertThrows(BusinessException.class, () -> local.encrypt("x"));
        assertEquals(ResultCode.CRYPTO_CONFIG_INVALID.getCode(), ex.getCode());
    }

    @Test
    void rejectsUnsafeKeyId() {
        CryptoPluginProperties props = baseProperties();
        props.setDefaultKeyId("bad.key");
        assertThrows(BusinessException.class, () -> CryptoFacades.create(props));
    }

    private static CryptoPluginProperties baseProperties() {
        byte[] defaultRaw = new byte[32];
        byte[] payRaw = new byte[32];
        for (int i = 0; i < 32; i++) {
            defaultRaw[i] = (byte) i;
            payRaw[i] = (byte) (32 - i);
        }
        CryptoPluginProperties properties = new CryptoPluginProperties();
        properties.setEnabled(true);
        properties.setRequireKeys(true);
        properties.setStrictModuleKey(true);
        properties.setDefaultAlgorithm(CryptoAlgorithms.AES_GCM_ENVELOPE);
        properties.setDefaultKeyId("default");
        properties.setDefaultKey(Base64.getEncoder().encodeToString(defaultRaw));
        CryptoPluginProperties.ModuleKeyProperties payKey = new CryptoPluginProperties.ModuleKeyProperties();
        payKey.setKeyId("pay");
        payKey.setKey(Base64.getEncoder().encodeToString(payRaw));
        properties.setModuleKeys(Map.of(CryptoModuleCodes.PAY, payKey));
        return properties;
    }
}
