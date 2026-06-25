package com.mtfm.deadman.plugin.im.tencent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;

import lombok.Data;

/**
 * 腾讯云 IM 插件配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.im-tencent")
public class ImTencentPluginProperties {

    /** 是否启用插件 */
    private boolean enabled = true;

    /** 是否启用 Mock（无 SecretKey 时自动 Mock） */
    private boolean mockEnabled = true;

    /** 腾讯云 IM SDKAppID */
    private Long sdkAppId;

    /** 腾讯云 IM SecretKey */
    private String secretKey;

    /** 调用 REST API 的管理员 Identifier */
    private String adminIdentifier = "administrator";

    /** UserSig 有效期（秒） */
    private long userSigExpireSeconds = 86_400L;

    /** IM UserID 模板，占位符：{realm}、{subjectId} */
    private String userIdTemplate = "{realm}_{subjectId}";

    /** 腾讯云 IM REST API 根地址 */
    private String apiBaseUrl = "https://console.tim.qq.com";

    /**
     * 是否应使用 Mock 网关。
     *
     * @return 是否 Mock
     */
    public boolean shouldUseMock() {
        if (mockEnabled) {
            return true;
        }
        return sdkAppId == null || sdkAppId <= 0 || !StringUtils.hasText(secretKey);
    }

    /**
     * 校验生产配置是否完整。
     */
    public void requireProductionConfig() {
        if (shouldUseMock()) {
            return;
        }
        if (sdkAppId == null || sdkAppId <= 0) {
            throw new BusinessException(ResultCode.IM_CONFIG_INVALID, "缺少腾讯云 IM SDKAppID");
        }
        if (!StringUtils.hasText(secretKey)) {
            throw new BusinessException(ResultCode.IM_CONFIG_INVALID, "缺少腾讯云 IM SecretKey");
        }
        if (!StringUtils.hasText(adminIdentifier)) {
            throw new BusinessException(ResultCode.IM_CONFIG_INVALID, "缺少腾讯云 IM 管理员 Identifier");
        }
    }
}
