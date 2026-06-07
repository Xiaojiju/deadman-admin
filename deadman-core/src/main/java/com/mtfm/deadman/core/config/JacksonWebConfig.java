package com.mtfm.deadman.core.config;

import com.mtfm.deadman.core.config.jackson.JacksonModules;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HTTP JSON 序列化配置：Long 等大整数以字符串输出，避免前端精度溢出。
 */
@Configuration
public class JacksonWebConfig {

    @Bean
    JsonMapperBuilderCustomizer jsSafeLongJsonCustomizer() {
        return builder -> builder.addModule(JacksonModules.jsSafeLongModule());
    }
}
