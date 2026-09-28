package com.mtfm.deadman.plugin.sms.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

import com.mtfm.deadman.plugin.sms.LoggingSmsSender;
import com.mtfm.deadman.plugin.sms.MemorySmsCodeStore;
import com.mtfm.deadman.plugin.sms.RedisSmsCodeStore;
import com.mtfm.deadman.plugin.sms.SmsCodeService;
import com.mtfm.deadman.plugin.sms.SmsCodeStore;
import com.mtfm.deadman.plugin.sms.SmsSender;
import com.mtfm.deadman.plugin.sms.config.SmsPluginProperties;

/**
 * 短信验证码插件自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(SmsPluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.sms", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DeadmanSmsPluginAutoConfiguration {

    /**
     * 有 Redis 时使用共享验证码存储。
     *
     * @param redisTemplate Redis 模板
     * @return Redis 存储
     */
    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    SmsCodeStore redisSmsCodeStore(RedisTemplate<String, Object> redisTemplate) {
        return new RedisSmsCodeStore(redisTemplate);
    }

    /**
     * 无 Redis 时使用进程内存储。
     *
     * @return 内存存储
     */
    @Bean
    @ConditionalOnMissingBean(SmsCodeStore.class)
    SmsCodeStore memorySmsCodeStore() {
        return new MemorySmsCodeStore();
    }

    /**
     * 默认短信发送器。业务侧提供自己的 {@link SmsSender} 后不再注册。
     *
     * @param properties 插件配置
     * @return 日志发送器
     */
    @Bean
    @ConditionalOnMissingBean(SmsSender.class)
    SmsSender loggingSmsSender(SmsPluginProperties properties) {
        return new LoggingSmsSender(properties);
    }

    /**
     * 验证码生成与校验。
     *
     * @param smsCodeStore 存储
     * @param smsSender 发送器
     * @param properties 插件配置
     * @return 验证码服务
     */
    @Bean
    SmsCodeService smsCodeService(SmsCodeStore smsCodeStore, SmsSender smsSender, SmsPluginProperties properties) {
        return new SmsCodeService(smsCodeStore, smsSender, properties);
    }
}
