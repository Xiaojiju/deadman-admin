package com.mtfm.deadman.plugin.wechat.miniprogram.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * 微信登录绑定列表，每项将注册一个独立的 {@link com.mtfm.deadman.security.authentication.provider.LoginProvider}。
     * <p>
     * 示例：同时为用户端与管理端开启微信登录：
     * <pre>
     * login-bindings:
     *   - group-id: client
     *   - group-id: admin
     * </pre>
     */
    private List<LoginBinding> loginBindings = defaultLoginBindings();

    /**
     * YAML 绑定项。
     */
    @Data
    public static class LoginBinding {

        /** 登录 Provider 组标识 */
        private String groupId;

        /** 登录路径段，为空时使用插件默认值 wechat-miniprogram */
        private String loginPathSegment;
    }

    /**
     * 解析为统一的登录绑定列表。
     *
     * @return 登录绑定
     */
    public List<WechatMiniprogramLoginBinding> resolveLoginBindings() {
        if (loginBindings == null || loginBindings.isEmpty()) {
            return defaultResolvedBindings();
        }
        List<WechatMiniprogramLoginBinding> resolved = new ArrayList<>();
        for (LoginBinding binding : loginBindings) {
            if (binding == null || binding.getGroupId() == null || binding.getGroupId().isBlank()) {
                continue;
            }
            resolved.add(new WechatMiniprogramLoginBinding(
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

    private static List<WechatMiniprogramLoginBinding> defaultResolvedBindings() {
        return List.of(new WechatMiniprogramLoginBinding("client", null));
    }
}
