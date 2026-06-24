package com.mtfm.deadman.core.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.mtfm.deadman.common.spi.CacheTtlContributor;
import com.mtfm.deadman.common.spi.NamedCacheTtl;
import com.mtfm.deadman.core.config.properties.DeadmanProperties;

import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

/**
 * Redis 序列化与 Spring Cache（生产/开发环境，test 环境见 {@link TestCacheConfig}）。
 */
@Configuration
@EnableCaching
@Profile("!test")
public class RedisConfig {

    @Bean
    public GenericJacksonJsonRedisSerializer redisJsonSerializer() {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        return GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(typeValidator)
                .enableSpringCacheNullValueSupport()
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory, GenericJacksonJsonRedisSerializer redisJsonSerializer) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(redisJsonSerializer);
        template.setHashValueSerializer(redisJsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            DeadmanProperties properties,
            GenericJacksonJsonRedisSerializer redisJsonSerializer,
            ObjectProvider<CacheTtlContributor> cacheTtlContributors) {
        RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(properties.getCache().getUserProfileTtl())
                .prefixCacheNameWith("deadman:cache:")
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisJsonSerializer));
        Map<String, RedisCacheConfiguration> namedConfigurations = new LinkedHashMap<>();
        for (CacheTtlContributor contributor : cacheTtlContributors) {
            for (NamedCacheTtl namedCacheTtl : contributor.contribute()) {
                namedConfigurations.put(
                        namedCacheTtl.cacheName(),
                        defaultConfiguration.entryTtl(namedCacheTtl.ttl()));
            }
        }
        RedisCacheManager.RedisCacheManagerBuilder builder =
                RedisCacheManager.builder(connectionFactory).cacheDefaults(defaultConfiguration);
        if (!namedConfigurations.isEmpty()) {
            builder.withInitialCacheConfigurations(namedConfigurations);
        }
        return builder.build();
    }
}
