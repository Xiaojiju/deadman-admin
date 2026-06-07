package com.mtfm.deadman.plugin.wechat.miniprogram.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 微信小程序插件配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.wechat-miniprogram")
public class WechatMiniprogramPluginProperties {

    /** 是否启用插件 */
    private boolean enabled = true;

    /** 小程序 AppId */
    private String appId;

    /** 小程序 AppSecret */
    private String appSecret;

    /** 微信 API 基础地址 */
    private String apiBaseUrl = "https://api.weixin.qq.com";
}
