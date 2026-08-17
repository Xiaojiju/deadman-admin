package com.mtfm.deadman.plugin.pay.facade;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.pay.config.PayPluginProperties;
import com.mtfm.deadman.plugin.pay.manager.SubMerchantProviderManager;
import com.mtfm.deadman.plugin.pay.spi.merchant.SubMerchantApplyCommand;
import com.mtfm.deadman.plugin.pay.spi.merchant.SubMerchantApplyResult;
import com.mtfm.deadman.plugin.pay.spi.merchant.SubMerchantMediaUploadResult;
import com.mtfm.deadman.plugin.pay.spi.merchant.SubMerchantProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 二级商户（入驻商户）门面：进件 / 查询 / 手工绑号，渠道无关。
 * <p>
 * {@code deadman.plugin.pay.test-mode.sub-merchant.enabled=true} 时不调真实渠道，直接模拟进件与媒体上传。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubMerchantFacade {

    private final SubMerchantProviderManager subMerchantProviderManager;
    private final PayPluginProperties payPluginProperties;

    /**
     * 提交进件申请。
     *
     * @param providerId Provider 标识，空则取默认
     * @param command    进件命令
     * @return 进件结果
     */
    public SubMerchantApplyResult submitApplyment(String providerId, SubMerchantApplyCommand command) {
        if (command == null || !StringUtils.hasText(command.outRequestNo())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "进件申请单号不能为空");
        }
        if (isSubMerchantTestMode()) {
            log.info("进件 test-mode：模拟二级商户进件提交 outRequestNo={}", command.outRequestNo());
            return mockFinishedResult(command.outRequestNo().trim());
        }
        try {
            return require(providerId).submitApplyment(command);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.PAY_SUB_MERCHANT_APPLY_FAILED, "二级商户进件失败：" + ex.getMessage());
        }
    }

    /**
     * 查询进件状态。
     *
     * @param providerId   Provider 标识
     * @param outRequestNo 业务申请单号
     * @return 查询结果
     */
    public SubMerchantApplyResult queryApplyment(String providerId, String outRequestNo) {
        if (!StringUtils.hasText(outRequestNo)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "进件申请单号不能为空");
        }
        if (isSubMerchantTestMode()) {
            log.info("进件 test-mode：模拟二级商户进件查单 outRequestNo={}", outRequestNo);
            return mockFinishedResult(outRequestNo.trim());
        }
        return require(providerId).queryApplyment(outRequestNo.trim());
    }

    /**
     * 上传进件媒体文件，获取渠道 MediaID。
     *
     * @param providerId Provider 标识
     * @param fileName   文件名
     * @param content    文件内容
     * @return 上传结果
     */
    public SubMerchantMediaUploadResult uploadMedia(String providerId, String fileName, byte[] content) {
        if (isSubMerchantTestMode()) {
            log.info("进件 test-mode：模拟进件媒体上传 fileName={}, size={}",
                fileName, content == null ? 0 : content.length);
            return new SubMerchantMediaUploadResult("TEST_media_" + shortId());
        }
        try {
            return require(providerId).uploadMedia(fileName, content);
        } catch (BusinessException ex) {
            throw ex;
        } catch (UnsupportedOperationException ex) {
            throw new BusinessException(ResultCode.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.PAY_MEDIA_UPLOAD_FAILED, "媒体文件上传失败：" + ex.getMessage());
        }
    }

    /**
     * 手工绑定已开通的二级商户号（联调 / 存量商户 MVP，不调渠道）。
     *
     * @param outRequestNo 业务申请单号
     * @param subMchid     二级商户号
     * @return 绑定结果（状态 FINISH）
     */
    public SubMerchantApplyResult bindExternalSubMchid(String outRequestNo, String subMchid) {
        if (!StringUtils.hasText(outRequestNo)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "进件申请单号不能为空");
        }
        if (!StringUtils.hasText(subMchid)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "二级商户号不能为空");
        }
        return new SubMerchantApplyResult(
                outRequestNo.trim(), null, subMchid.trim(), "FINISH", null);
    }

    /**
     * 列出已注册 Provider。
     *
     * @return 标识列表
     */
    public java.util.List<String> listProviders() {
        return subMerchantProviderManager.listProviderIds();
    }

    private SubMerchantProvider require(String providerId) {
        return subMerchantProviderManager.require(providerId);
    }

    private boolean isSubMerchantTestMode() {
        return payPluginProperties.isSubMerchantTestModeEnabled();
    }

    private static SubMerchantApplyResult mockFinishedResult(String outRequestNo) {
        return new SubMerchantApplyResult(
            outRequestNo,
            "TEST_apply_" + shortId(),
            "TEST" + shortId().substring(0, 8),
            "APPLYMENT_STATE_FINISHED",
            null);
    }

    private static String shortId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
