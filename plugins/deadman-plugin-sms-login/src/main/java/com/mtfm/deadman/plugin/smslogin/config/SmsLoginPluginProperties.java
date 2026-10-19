package com.mtfm.deadman.plugin.smslogin.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 短信验证码登录插件配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.plugin.sms-login")
public class SmsLoginPluginProperties {

    /** 是否启用插件 */
    private boolean enabled = true;

    /**
     * 登录绑定。每项注册一个 {@link com.mtfm.deadman.security.authentication.provider.LoginProvider}。 默认挂到用户端组，路径段
     * {@code sms}，完整路径由该组的认证前缀拼接。
     */
    private List<LoginBinding> loginBindings = defaultLoginBindings();

    /**
     * YAML 绑定项。
     */
    @Data
    public static class LoginBinding {

        /** 登录 Provider 组标识 */
        private String groupId;

        /** 登录路径段，为空时使用 sms */
        private String loginPathSegment;
    }

    /**
     * 解析为登录绑定列表。
     *
     * @return 登录绑定
     */
    public List<SmsLoginBinding> resolveLoginBindings() {
        if (loginBindings == null || loginBindings.isEmpty()) {
            return defaultResolvedBindings();
        }
        List<SmsLoginBinding> resolved = new ArrayList<>();
        for (LoginBinding binding : loginBindings) {
            if (binding == null || binding.getGroupId() == null || binding.getGroupId().isBlank()) {
                continue;
            }
            String segment = binding.getLoginPathSegment();
            resolved.add(new SmsLoginBinding(binding.getGroupId().trim(),
                segment == null || segment.isBlank() ? null : segment.trim()));
        }
        return resolved.isEmpty() ? defaultResolvedBindings() : List.copyOf(resolved);
    }

    private static List<LoginBinding> defaultLoginBindings() {
        LoginBinding client = new LoginBinding();
        client.setGroupId("client");
        client.setLoginPathSegment("sms");
        return new ArrayList<>(List.of(client));
    }

    private static List<SmsLoginBinding> defaultResolvedBindings() {
        return List.of(new SmsLoginBinding("client", "sms"));
    }
}
