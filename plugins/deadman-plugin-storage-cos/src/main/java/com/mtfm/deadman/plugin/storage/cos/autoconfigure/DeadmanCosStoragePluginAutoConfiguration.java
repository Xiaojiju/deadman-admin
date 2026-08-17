package com.mtfm.deadman.plugin.storage.cos.autoconfigure;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.plugin.storage.cos.config.CosStoragePluginProperties;

/**
 * 腾讯云 COS 存储插件自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(CosStoragePluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.storage-cos", name = "enabled", havingValue = "true", matchIfMissing = false)
@ComponentScan(basePackages = "com.mtfm.deadman.plugin.storage.cos")
public class DeadmanCosStoragePluginAutoConfiguration {

    /**
     * 兼容 YAML {@code key: {}} 被展平为空字符串的情况，避免 Map 绑定失败。
     *
     * @return 空串 → 空 Map 转换器
     */
    @Bean
    @ConfigurationPropertiesBinding
    public Converter<String, Map<String, String>> cosBlankStringToStringMapConverter() {
        return source -> {
            if (!StringUtils.hasText(source) || "{}".equals(source.trim())) {
                return new HashMap<>();
            }
            throw new IllegalArgumentException(
                "无法将非空字符串绑定为 Map<String,String>，请使用 YAML 嵌套写法");
        };
    }
}
