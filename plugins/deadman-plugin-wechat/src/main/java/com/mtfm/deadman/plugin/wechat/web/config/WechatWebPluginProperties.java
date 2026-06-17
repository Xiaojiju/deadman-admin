package com.mtfm.deadman.plugin.wechat.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 微信网页扫码登录插件配置（开放平台网站应用）。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.wechat-web")
public class WechatWebPluginProperties {

    /** 是否启用插件 */
    private boolean enabled = false;

    /** 开放平台网站应用 AppId */
    private String appId;

    /** 开放平台网站应用 AppSecret */
    private String appSecret;

    /**
     * 授权回调地址，须与微信开放平台配置一致。
     * 用户扫码后微信将携带 code 重定向到此地址。
     */
    private String redirectUri;

    /** 微信 API 基础地址 */
    private String apiBaseUrl = "https://api.weixin.qq.com";

    /** OAuth state 有效期，用于防 CSRF */
    private Duration stateTtl = Duration.ofMinutes(5);

    /**
     * 微信登录绑定列表，每项将注册一个独立的
     * {@link com.mtfm.deadman.security.authentication.provider.LoginProvider}。
     */
    private List<LoginBinding> loginBindings = defaultLoginBindings();

    /**
     * YAML 绑定项。
     */
    @Data
    public static class LoginBinding {

        /** 登录 Provider 组标识 */
        private String groupId;

        /** 登录路径段，为空时使用插件默认值 wechat-web */
        private String loginPathSegment;
    }

    /**
     * 解析为统一的登录绑定列表。
     *
     * @return 登录绑定
     */
    public List<WechatWebLoginBinding> resolveLoginBindings() {
        if (loginBindings == null || loginBindings.isEmpty()) {
            return defaultResolvedBindings();
        }
        List<WechatWebLoginBinding> resolved = new ArrayList<>();
        for (LoginBinding binding : loginBindings) {
            if (binding == null || binding.getGroupId() == null || binding.getGroupId().isBlank()) {
                continue;
            }
            resolved.add(new WechatWebLoginBinding(
                    binding.getGroupId().trim(),
                    binding.getLoginPathSegment() == null || binding.getLoginPathSegment().isBlank()
                            ? null
                            : binding.getLoginPathSegment().trim()));
        }
        return resolved.isEmpty() ? defaultResolvedBindings() : List.copyOf(resolved);
    }

    private static List<LoginBinding> defaultLoginBindings() {
        LoginBinding client = new LoginBinding();
        client.setGroupId("client");
        return new ArrayList<>(List.of(client));
    }

    private static List<WechatWebLoginBinding> defaultResolvedBindings() {
        return List.of(new WechatWebLoginBinding("client", null));
    }
}
