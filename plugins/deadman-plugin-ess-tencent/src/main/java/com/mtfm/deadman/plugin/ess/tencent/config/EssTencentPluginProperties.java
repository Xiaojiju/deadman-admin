package com.mtfm.deadman.plugin.ess.tencent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;

import lombok.Data;

/**
 * 腾讯电子签插件配置，支持企业版（ess）与渠道版（essbasic）两种接入模式。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.ess-tencent")
public class EssTencentPluginProperties {

    /** 接入模式：enterprise（企业版 ess，默认）或 channel（渠道版 essbasic） */
    private String mode = "enterprise";

    /** 是否启用插件 */
    private boolean enabled = false;

    /** 腾讯云 SecretId */
    private String secretId;

    /** 腾讯云 SecretKey */
    private String secretKey;

    /** 渠道版 AppId（mode=channel 时必填） */
    private String appId;

    /**
     * 经办人 UserId（电子签控制台管理员或员工 UserId）。
     * 发起合同、查询、撤销等接口均需此操作人。
     */
    private String operatorUserId;

    /** API 接入地域，电子签固定为 ap-guangzhou */
    private String region = "ap-guangzhou";

    /**
     * 企业版电子签 API 域名。
     * 现网：ess.tencentcloudapi.com；测试：ess.test.ess.tencent.cn
     */
    private String endpoint = "ess.tencentcloudapi.com";

    /**
     * 渠道版电子签 API 域名（除 UploadFiles 外）。
     * 联调：essbasic.test.ess.tencent.cn；现网：essbasic.tencentcloudapi.com
     */
    private String channelEndpoint = "essbasic.tencentcloudapi.com";

    /**
     * 文件服务域名（UploadFiles 专用）。
     * 联调：file.test.ess.tencent.cn；现网：file.ess.tencent.cn
     */
    private String fileEndpoint = "file.ess.tencent.cn";

    /** 渠道版平台统一模板 Id（可选；业务侧也可在命令中传入 templateId） */
    private String templateId;

    /** 企业静默签默认印章 Id（可选） */
    private String serverSignSealId;

    /** 回调签名 Token（Content-Signature 校验用，可选） */
    private String callbackToken;

    /** 回调 AES 密钥（回调解密用，可选） */
    private String callbackAesKey;

    /**
     * 本应用接收腾讯电子签回调的 endpoint 路径（公开 POST）。
     * <p>
     * 默认 {@code /api/ess/tencent/callback}；可在腾讯电子签控制台配置为完整 URL。
     */
    private String callbackEndpoint = "/api/ess/tencent/callback";

    /** HTTP 连接超时秒数 */
    private int connTimeoutSeconds = 30;

    /**
     * 规范化回调 endpoint（保证前导斜杠、去掉尾斜杠）。
     *
     * @return 规范化路径
     */
    public String resolveCallbackEndpoint() {
        String endpoint = StringUtils.hasText(callbackEndpoint) ? callbackEndpoint.trim() : "/api/ess/tencent/callback";
        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }
        if (endpoint.endsWith("/") && endpoint.length() > 1) {
            return endpoint.substring(0, endpoint.length() - 1);
        }
        return endpoint;
    }

    /**
     * 是否为渠道版（essbasic）接入模式。
     *
     * @return true 表示渠道版
     */
    public boolean isChannelMode() {
        return "channel".equalsIgnoreCase(mode);
    }

    /**
     * 解析渠道版 API 域名。
     *
     * @return 渠道版 endpoint
     */
    public String resolveChannelEndpoint() {
        if (StringUtils.hasText(channelEndpoint)) {
            return channelEndpoint.trim();
        }
        return "essbasic.tencentcloudapi.com";
    }

    /**
     * 校验渠道版配置是否完整（SecretId、SecretKey、AppId）。
     */
    public void requireChannelConfig() {
        if (!StringUtils.hasText(secretId)) {
            throw new BusinessException(ResultCode.ESS_CONFIG_INVALID, "缺少腾讯电子签 SecretId");
        }
        if (!StringUtils.hasText(secretKey)) {
            throw new BusinessException(ResultCode.ESS_CONFIG_INVALID, "缺少腾讯电子签 SecretKey");
        }
        if (!StringUtils.hasText(appId)) {
            throw new BusinessException(ResultCode.ESS_CONFIG_INVALID, "缺少腾讯电子签渠道 AppId");
        }
        if (!StringUtils.hasText(resolveChannelEndpoint())) {
            throw new BusinessException(ResultCode.ESS_CONFIG_INVALID, "缺少腾讯电子签渠道 API Endpoint");
        }
    }

    /**
     * 解析有效模板 Id：优先使用入参，否则回落到配置默认值。
     *
     * @param templateId 可选模板 Id
     * @return 模板 Id
     */
    public String resolveTemplateId(String templateId) {
        if (StringUtils.hasText(templateId)) {
            return templateId;
        }
        if (StringUtils.hasText(this.templateId)) {
            return this.templateId;
        }
        throw new BusinessException(ResultCode.ESS_CONFIG_INVALID, "缺少腾讯电子签模板 TemplateId");
    }

    /**
     * 校验企业版配置是否完整。
     */
    public void requireProductionConfig() {
        if (!StringUtils.hasText(secretId)) {
            throw new BusinessException(ResultCode.ESS_CONFIG_INVALID, "缺少腾讯电子签 SecretId");
        }
        if (!StringUtils.hasText(secretKey)) {
            throw new BusinessException(ResultCode.ESS_CONFIG_INVALID, "缺少腾讯电子签 SecretKey");
        }
        if (!StringUtils.hasText(operatorUserId)) {
            throw new BusinessException(ResultCode.ESS_CONFIG_INVALID, "缺少腾讯电子签经办人 OperatorUserId");
        }
        if (!StringUtils.hasText(endpoint)) {
            throw new BusinessException(ResultCode.ESS_CONFIG_INVALID, "缺少腾讯电子签 API Endpoint");
        }
        if (!StringUtils.hasText(fileEndpoint)) {
            throw new BusinessException(ResultCode.ESS_CONFIG_INVALID, "缺少腾讯电子签文件服务 Endpoint");
        }
    }

    /**
     * 解析有效经办人 UserId：优先使用入参，否则回落到配置默认值。
     *
     * @param operatorUserId 可选经办人
     * @return 经办人 UserId
     */
    public String resolveOperatorUserId(String operatorUserId) {
        if (StringUtils.hasText(operatorUserId)) {
            return operatorUserId;
        }
        if (!StringUtils.hasText(this.operatorUserId)) {
            throw new BusinessException(ResultCode.ESS_CONFIG_INVALID, "缺少腾讯电子签经办人 OperatorUserId");
        }
        return this.operatorUserId;
    }
}
