package com.mtfm.deadman.plugin.pay.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.crypto.constant.CryptoModuleCodes;
import com.mtfm.deadman.plugin.crypto.facade.CryptoFacade;
import com.mtfm.deadman.plugin.crypto.spi.CryptoContext;
import com.mtfm.deadman.plugin.crypto.util.CryptoPayloadCodec;
import com.mtfm.deadman.plugin.pay.constant.PayCryptoPurposes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 支付敏感字段加解密助手（如转账收款人姓名）。
 *
 * <p>强制依赖 {@link CryptoFacade}：未装配时拒绝加解密，避免静默明文落库。
 * 加密使用 pay 模块密钥 + {@link PayCryptoPurposes#TRANSFER_USER_NAME} AAD 绑定。
 *
 * @see CryptoFacade
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaySensitiveCryptoHelper {

    /** 转账姓名加解密上下文（模块 + 用途） */
    private static final CryptoContext TRANSFER_USER_NAME_CTX =
            CryptoContext.forModule(CryptoModuleCodes.PAY).withPurpose(PayCryptoPurposes.TRANSFER_USER_NAME);

    /** 可选的加解密门面；未装配时加解密均失败 */
    private final ObjectProvider<CryptoFacade> cryptoFacadeProvider;

    /**
     * 加密收款人姓名（pay 模块密钥 + 字段用途 AAD）。
     *
     * @param userName 明文姓名
     * @return 密文 token；空值原样返回
     */
    public String encryptUserName(String userName) {
        if (!StringUtils.hasText(userName)) {
            return userName;
        }
        return requireFacade().encrypt(userName.trim(), TRANSFER_USER_NAME_CTX);
    }

    /**
     * 解密收款人姓名；兼容历史明文与审查前无 AAD 密文。
     *
     * @param stored 库中存储值（密文或历史明文）
     * @return 明文姓名
     */
    public String decryptUserName(String stored) {
        if (!StringUtils.hasText(stored)) {
            return stored;
        }
        CryptoFacade facade = requireFacade();
        if (!CryptoPayloadCodec.isEncryptedToken(stored)) {
            return stored;
        }
        try {
            return facade.decrypt(stored, TRANSFER_USER_NAME_CTX);
        } catch (BusinessException ex) {
            // 兼容审查前无 purpose AAD 的密文
            if (ex.getCode() == ResultCode.CRYPTO_DECRYPT_FAILED.getCode()) {
                log.warn("转账姓名按带 AAD 上下文解密失败，尝试无 AAD 兼容解密");
                return facade.decrypt(stored);
            }
            throw ex;
        }
    }

    /**
     * 获取门面；未装配则 fail-closed。
     *
     * @return 加解密门面
     */
    private CryptoFacade requireFacade() {
        CryptoFacade facade = cryptoFacadeProvider.getIfAvailable();
        if (facade == null) {
            throw new BusinessException(
                    ResultCode.CRYPTO_CONFIG_INVALID,
                    "CryptoFacade 未装配：转账姓名禁止明文存储，请启用 deadman-extension-crypto 并配置密钥");
        }
        return facade;
    }
}
