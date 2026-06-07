package com.mtfm.deadman.core.config;

import com.mtfm.deadman.core.config.properties.DeadmanProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DeadmanProperties.class)
public class DeadmanPropertiesConfig {
}
