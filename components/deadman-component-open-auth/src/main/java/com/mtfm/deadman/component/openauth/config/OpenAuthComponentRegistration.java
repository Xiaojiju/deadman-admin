package com.mtfm.deadman.component.openauth.config;

import com.mtfm.deadman.core.component.DeadmanComponentDescriptor;
import com.mtfm.deadman.core.component.vo.DeadmanComponentUiHints;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 开放授权组件向核心注册表贡献描述信息。
 */
@Configuration
@ConditionalOnProperty(prefix = "deadman.component.open-auth", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OpenAuthComponentRegistration {

    /**
     * 注册开放授权组件描述符。
     *
     * @return 组件描述符
     */
    @Bean
    DeadmanComponentDescriptor openAuthDeadmanComponentDescriptor() {
        return new DeadmanComponentDescriptor(
                "open-auth",
                "开放授权",
                "第三方应用注册、授权码签发与 access_token 兑换",
                "/open-api",
                110,
                new DeadmanComponentUiHints("/api/open-apps", List.of("open-app")));
    }
}
