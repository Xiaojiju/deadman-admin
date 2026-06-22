package com.mtfm.deadman.component.client.config;

import com.mtfm.deadman.core.component.DeadmanComponentDescriptor;
import com.mtfm.deadman.core.component.vo.DeadmanComponentUiHints;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 用户端组件向核心注册表贡献描述信息。
 */
@Configuration
@ConditionalOnProperty(prefix = "deadman.component.client", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ClientComponentRegistration {

    /**
     * 注册用户端组件描述符。
     *
     * @param properties 用户端组件配置
     * @return 组件描述符
     */
    @Bean
    DeadmanComponentDescriptor clientDeadmanComponentDescriptor(ClientComponentProperties properties) {
        return new DeadmanComponentDescriptor(
                "client",
                "用户端",
                "独立用户体系：注册、登录与个人中心",
                "/client/api",
                100,
                new DeadmanComponentUiHints(properties.getAuth().getBasePath(), List.of("auth", "profile")));
    }
}
