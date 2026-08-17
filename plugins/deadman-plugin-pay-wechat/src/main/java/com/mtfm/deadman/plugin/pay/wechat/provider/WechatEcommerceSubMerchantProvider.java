package com.mtfm.deadman.plugin.pay.wechat.provider;

import java.util.Locale;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.constant.PaymentPlatform;
import com.mtfm.deadman.plugin.pay.spi.merchant.SubMerchantApplyCommand;
import com.mtfm.deadman.plugin.pay.spi.merchant.SubMerchantApplyResult;
import com.mtfm.deadman.plugin.pay.spi.merchant.SubMerchantChannelParams;
import com.mtfm.deadman.plugin.pay.spi.merchant.SubMerchantMediaUploadResult;
import com.mtfm.deadman.plugin.pay.spi.merchant.SubMerchantProvider;
import com.mtfm.deadman.plugin.pay.wechat.client.WechatPayApiGateway;
import com.mtfm.deadman.plugin.pay.wechat.constant.WechatPayProviderIds;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceApplymentCommand;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatEcommerceApplymentResult;
import com.mtfm.deadman.plugin.pay.wechat.vo.WechatMediaUploadResult;

import lombok.RequiredArgsConstructor;

/**
 * 微信收付通二级商户进件 Provider。
 * <p>
 * 校验规则对齐官方「提交申请单」：始终必填账户/超管/场景/简称；
 * 小微与个人卖家不要求营业执照；大陆身份证路径要求证件 MediaID 与有效期。
 *
 * @see <a href="https://pay.weixin.qq.com/doc/v3/partner/4012713017">提交申请单</a>
 */
@Component
@ConditionalOnProperty(prefix = "deadman.plugin.pay-wechat", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class WechatEcommerceSubMerchantProvider implements SubMerchantProvider {

    /** 无需营业执照的主体类型：小微 / 个人卖家 */
    private static final Set<String> NO_LICENSE_ORG_TYPES = Set.of("2401", "2500");

    private final WechatPayApiGateway wechatPayApiGateway;

    @Override
    public String providerId() {
        return WechatPayProviderIds.WECHAT_ECOMMERCE_SUB_MERCHANT;
    }

    @Override
    public String payPlatform() {
        return PaymentPlatform.WECHAT;
    }

    @Override
    public SubMerchantApplyResult submitApplyment(SubMerchantApplyCommand command) {
        WechatEcommerceApplymentCommand wechatCommand = toWechatCommand(command);
        WechatEcommerceApplymentResult result = wechatPayApiGateway.createEcommerceApplyment(wechatCommand);
        return toResult(result);
    }

    @Override
    public SubMerchantApplyResult queryApplyment(String outRequestNo) {
        WechatEcommerceApplymentResult result = wechatPayApiGateway.queryEcommerceApplyment(outRequestNo);
        return toResult(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubMerchantMediaUploadResult uploadMedia(String fileName, byte[] content) {
        WechatMediaUploadResult result = wechatPayApiGateway.uploadMedia(fileName, content);
        if (result == null || !StringUtils.hasText(result.mediaId())) {
            throw new BusinessException(ResultCode.PAY_MEDIA_UPLOAD_FAILED, "微信媒体上传无 MediaID");
        }
        return new SubMerchantMediaUploadResult(result.mediaId());
    }

    private static WechatEcommerceApplymentCommand toWechatCommand(SubMerchantApplyCommand command) {
        if (command == null || !StringUtils.hasText(command.outRequestNo())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "二级商户进件缺少业务申请单号");
        }
        if (!StringUtils.hasText(command.organizationType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "二级商户进件缺少主体类型 organizationType");
        }
        if (!StringUtils.hasText(command.merchantShortname())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "二级商户进件缺少商户简称 merchantShortname");
        }

        requireParam(command, SubMerchantChannelParams.BANK_ACCOUNT_TYPE);
        requireParam(command, SubMerchantChannelParams.ACCOUNT_BANK);
        requireParam(command, SubMerchantChannelParams.ACCOUNT_NAME);
        requireParam(command, SubMerchantChannelParams.ACCOUNT_NUMBER);
        requireParam(command, SubMerchantChannelParams.CONTACT_TYPE);
        requireParam(command, SubMerchantChannelParams.CONTACT_NAME);
        requireParam(command, SubMerchantChannelParams.MOBILE_PHONE);
        requireParam(command, SubMerchantChannelParams.STORE_NAME);

        String storeUrl = trimToNull(command.param(SubMerchantChannelParams.STORE_URL));
        String storeQrCode = trimToNull(command.param(SubMerchantChannelParams.STORE_QR_CODE));
        if (!StringUtils.hasText(storeUrl) && !StringUtils.hasText(storeQrCode)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "二级商户进件须提供店铺链接或店铺二维码 MediaID");
        }

        String orgType = command.organizationType().trim();
        if (needsBusinessLicense(orgType)) {
            requireParam(command, SubMerchantChannelParams.BUSINESS_LICENSE_COPY);
            requireParam(command, SubMerchantChannelParams.BUSINESS_LICENSE_NUMBER);
            requireParam(command, SubMerchantChannelParams.BUSINESS_LICENSE_MERCHANT_NAME);
            requireParam(command, SubMerchantChannelParams.BUSINESS_LICENSE_LEGAL_PERSON);
        }

        // 当前实现仅支持大陆身份证路径（id_card_info）
        requireParam(command, SubMerchantChannelParams.ID_CARD_COPY);
        requireParam(command, SubMerchantChannelParams.ID_CARD_NATIONAL);
        requireParam(command, SubMerchantChannelParams.ID_CARD_NAME);
        requireParam(command, SubMerchantChannelParams.ID_CARD_NUMBER);
        requireParam(command, SubMerchantChannelParams.ID_CARD_VALID_TIME_BEGIN);
        requireParam(command, SubMerchantChannelParams.ID_CARD_VALID_TIME);

        return new WechatEcommerceApplymentCommand(
                command.outRequestNo().trim(),
                orgType,
                command.merchantShortname().trim(),
                command.param(SubMerchantChannelParams.BANK_ACCOUNT_TYPE),
                command.param(SubMerchantChannelParams.ACCOUNT_BANK),
                command.param(SubMerchantChannelParams.ACCOUNT_NAME),
                command.param(SubMerchantChannelParams.ACCOUNT_NUMBER),
                command.param(SubMerchantChannelParams.CONTACT_TYPE),
                command.param(SubMerchantChannelParams.CONTACT_NAME),
                command.param(SubMerchantChannelParams.MOBILE_PHONE),
                trimToNull(command.param(SubMerchantChannelParams.CONTACT_ID_CARD_NUMBER)),
                command.param(SubMerchantChannelParams.STORE_NAME),
                storeUrl,
                storeQrCode,
                trimToNull(command.param(SubMerchantChannelParams.BUSINESS_LICENSE_COPY)),
                trimToNull(command.param(SubMerchantChannelParams.BUSINESS_LICENSE_NUMBER)),
                trimToNull(command.param(SubMerchantChannelParams.BUSINESS_LICENSE_MERCHANT_NAME)),
                trimToNull(command.param(SubMerchantChannelParams.BUSINESS_LICENSE_LEGAL_PERSON)),
                command.param(SubMerchantChannelParams.ID_CARD_COPY),
                command.param(SubMerchantChannelParams.ID_CARD_NATIONAL),
                command.param(SubMerchantChannelParams.ID_CARD_NAME),
                command.param(SubMerchantChannelParams.ID_CARD_NUMBER),
                command.param(SubMerchantChannelParams.ID_CARD_VALID_TIME_BEGIN),
                command.param(SubMerchantChannelParams.ID_CARD_VALID_TIME));
    }

    private static boolean needsBusinessLicense(String organizationType) {
        return !NO_LICENSE_ORG_TYPES.contains(organizationType.toLowerCase(Locale.ROOT))
                && !NO_LICENSE_ORG_TYPES.contains(organizationType);
    }

    private static void requireParam(SubMerchantApplyCommand command, String key) {
        if (!StringUtils.hasText(command.param(key))) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "二级商户进件缺少参数：" + key);
        }
    }

    private static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private static SubMerchantApplyResult toResult(WechatEcommerceApplymentResult result) {
        if (result == null) {
            throw new BusinessException(ResultCode.PAY_SUB_MERCHANT_APPLY_FAILED, "微信进件无响应");
        }
        return new SubMerchantApplyResult(
                result.outRequestNo(),
                result.applymentId(),
                result.subMchid(),
                result.applymentState(),
                result.signUrl());
    }
}
