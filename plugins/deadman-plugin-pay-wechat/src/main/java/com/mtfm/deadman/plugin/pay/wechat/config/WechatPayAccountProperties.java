package com.mtfm.deadman.plugin.pay.wechat.config;

import java.util.Objects;

import org.springframework.util.StringUtils;

import lombok.Data;

/**
 * 一套微信支付商户凭证（普通直连商户或合作伙伴/受理机构）。
 */
@Data
public class WechatPayAccountProperties {

    /** 微信支付商户号（直连 mchid 或合作伙伴 sp_mchid） */
    private String mchId;

    /**
     * 合作伙伴/平台商户号；仅收付通合单等场景使用，为空时回退 {@link #mchId}。
     */
    private String partnerMchid;

    /** APIv3 密钥 */
    private String apiV3Key;

    /** 商户 API 证书序列号 */
    private String merchantSerialNo;

    /** 商户 API 私钥 PEM 文件路径 */
    private String privateKeyPath;

    /**
     * 微信支付公钥 PEM 文件路径（验签用，与 {@link #publicKeyId} 成对配置）。
     */
    private String publicKeyPath;

    /** 微信支付公钥 ID，形如 {@code PUB_KEY_ID_...} */
    private String publicKeyId;

    /**
     * 解析合单/收付通使用的平台商户号。
     *
     * @return 平台商户号
     */
    public String resolvePartnerMchid() {
        return StringUtils.hasText(partnerMchid) ? partnerMchid.trim() : trimToNull(mchId);
    }

    /**
     * 核心下单凭证是否齐全。
     *
     * @return 商户号、APIv3 密钥、证书序列号、私钥路径均已配置
     */
    public boolean hasCoreCredentials() {
        return StringUtils.hasText(mchId)
                && StringUtils.hasText(apiV3Key)
                && StringUtils.hasText(merchantSerialNo)
                && StringUtils.hasText(privateKeyPath);
    }

    /**
     * 是否填写了任一凭证字段（用于判断是否显式配置了该账户）。
     *
     * @return 任一凭证字段非空
     */
    public boolean hasAnyCredential() {
        return StringUtils.hasText(mchId)
                || StringUtils.hasText(partnerMchid)
                || StringUtils.hasText(apiV3Key)
                || StringUtils.hasText(merchantSerialNo)
                || StringUtils.hasText(privateKeyPath)
                || StringUtils.hasText(publicKeyPath)
                || StringUtils.hasText(publicKeyId);
    }

    /**
     * 是否使用微信支付公钥验签。
     *
     * @return 公钥路径与公钥 ID 均已配置
     */
    public boolean usePublicKeyVerifier() {
        return StringUtils.hasText(publicKeyPath) && StringUtils.hasText(publicKeyId);
    }

    /**
     * 校验真实网关所需凭证齐全。
     *
     * @param accountLabel 账户标识（写入异常信息）
     */
    public void requireComplete(String accountLabel) {
        if (!hasCoreCredentials()) {
            throw new IllegalStateException(
                    "微信支付账户「" + accountLabel + "」缺少 mchId/apiV3Key/merchantSerialNo/privateKeyPath");
        }
        boolean hasPublicKeyPath = StringUtils.hasText(publicKeyPath);
        boolean hasPublicKeyId = StringUtils.hasText(publicKeyId);
        if (hasPublicKeyPath != hasPublicKeyId) {
            throw new IllegalStateException(
                    "微信支付账户「" + accountLabel + "」公钥模式需同时配置 publicKeyPath 与 publicKeyId");
        }
    }

    /**
     * 用另一套凭证填补当前空字段（用于顶层旧配置回退到 ordinary）。
     *
     * @param fallback 回退凭证
     */
    public void fillBlanksFrom(WechatPayAccountProperties fallback) {
        if (fallback == null) {
            return;
        }
        if (!StringUtils.hasText(mchId)) {
            mchId = fallback.getMchId();
        }
        if (!StringUtils.hasText(partnerMchid)) {
            partnerMchid = fallback.getPartnerMchid();
        }
        if (!StringUtils.hasText(apiV3Key)) {
            apiV3Key = fallback.getApiV3Key();
        }
        if (!StringUtils.hasText(merchantSerialNo)) {
            merchantSerialNo = fallback.getMerchantSerialNo();
        }
        if (!StringUtils.hasText(privateKeyPath)) {
            privateKeyPath = fallback.getPrivateKeyPath();
        }
        if (!StringUtils.hasText(publicKeyPath)) {
            publicKeyPath = fallback.getPublicKeyPath();
        }
        if (!StringUtils.hasText(publicKeyId)) {
            publicKeyId = fallback.getPublicKeyId();
        }
    }

    /**
     * 复制一套凭证。
     *
     * @return 副本
     */
    public WechatPayAccountProperties copy() {
        WechatPayAccountProperties copy = new WechatPayAccountProperties();
        copy.fillBlanksFrom(this);
        return copy;
    }

    /**
     * 是否与另一套凭证指向同一商户身份（用于复用 SDK 客户端）。
     *
     * @param other 另一套凭证
     * @return 商户号与私钥路径相同
     */
    public boolean sameMerchantIdentity(WechatPayAccountProperties other) {
        if (other == null) {
            return false;
        }
        return Objects.equals(trimToNull(mchId), trimToNull(other.getMchId()))
                && Objects.equals(trimToNull(privateKeyPath), trimToNull(other.getPrivateKeyPath()));
    }

    private static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
