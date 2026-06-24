package com.mtfm.deadman.plugin.logistics.kuaidi100.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;

import lombok.Data;

/**
 * 快递100 物流插件配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.logistics-kuaidi100")
public class Kuaidi100LogisticsPluginProperties {

    /** 是否启用快递100 Provider */
    private boolean enabled = false;

    /** 是否使用 Mock 网关（无 key/customer 时可用于本地验收） */
    private boolean mockEnabled = true;

    /** 快递100 授权 key */
    private String key = "";

    /** 快递100 授权 customer（实时查单） */
    private String customer = "";

    /** 快递100 授权 secret（面单、寄件等接口） */
    private String secret = "";

    /** 轨迹订阅推送验签 salt */
    private String subscribeSalt = "";

    /** 轨迹订阅推送 HTTP 回调 endpoint（本应用接收） */
    private String subscribeNotifyEndpoint = "/client/api/logistics/kuaidi100/subscribe/notify";

    /** 轨迹订阅默认回调 URL（传给快递100，为空时使用 subscribeNotifyEndpoint 拼接公网域名需业务侧配置完整 URL） */
    private String subscribeCallbackUrl = "";

    /** 商家寄件默认回调 URL */
    private String merchantShipCallbackUrl = "";

    /** C 端寄件默认回调 URL */
    private String consumerShipCallbackUrl = "";

    /**
     * 是否应使用 Mock 网关。
     *
     * @return true 表示使用 Mock
     */
    public boolean shouldUseMock() {
        if (mockEnabled) {
            return true;
        }
        return !StringUtils.hasText(key) || !StringUtils.hasText(customer);
    }

    /**
     * 校验 secret 已配置（面单、寄件等需 printSign 的接口必需）。
     */
    public void requireSecret() {
        if (!StringUtils.hasText(key) || !StringUtils.hasText(secret)) {
            throw new BusinessException(ResultCode.LOGISTICS_CONFIG_INVALID, "快递100 key 或 secret 未配置");
        }
    }

    /**
     * 解析轨迹订阅推送 endpoint 路径。
     *
     * @return 标准化后的 endpoint
     */
    public String resolvedSubscribeNotifyEndpoint() {
        if (!StringUtils.hasText(subscribeNotifyEndpoint)) {
            return "/client/api/logistics/kuaidi100/subscribe/notify";
        }
        return subscribeNotifyEndpoint.startsWith("/") ? subscribeNotifyEndpoint : "/" + subscribeNotifyEndpoint;
    }

    /**
     * 解析轨迹订阅回调 URL，优先使用入参，其次插件默认配置。
     *
     * @param contextCallbackUrl 业务入参回调地址
     * @return 最终回调 URL
     */
    public String resolveSubscribeCallbackUrl(String contextCallbackUrl) {
        if (StringUtils.hasText(contextCallbackUrl)) {
            return contextCallbackUrl;
        }
        return subscribeCallbackUrl;
    }

    /**
     * 解析商家寄件回调 URL。
     *
     * @param contextCallbackUrl 业务入参回调地址
     * @return 最终回调 URL
     */
    public String resolveMerchantShipCallbackUrl(String contextCallbackUrl) {
        if (StringUtils.hasText(contextCallbackUrl)) {
            return contextCallbackUrl;
        }
        return merchantShipCallbackUrl;
    }

    /**
     * 解析 C 端寄件回调 URL。
     *
     * @param contextCallbackUrl 业务入参回调地址
     * @return 最终回调 URL
     */
    public String resolveConsumerShipCallbackUrl(String contextCallbackUrl) {
        if (StringUtils.hasText(contextCallbackUrl)) {
            return contextCallbackUrl;
        }
        return consumerShipCallbackUrl;
    }
}
