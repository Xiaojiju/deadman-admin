package com.mtfm.deadman.core.config;

import com.mtfm.deadman.common.constants.PasswordEncoderIds;
import com.mtfm.deadman.core.password.PasswordEncoderRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import java.util.List;

/**
 * 注册多种 PasswordEncoder，供 {@link PasswordEncoderRegistry} 按用户随机选用。
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoderRegistry passwordEncoderRegistry() {
        List<PasswordEncoderRegistry.PasswordEncoderDefinition> definitions = List.of(
                new PasswordEncoderRegistry.PasswordEncoderDefinition(
                        PasswordEncoderIds.BCRYPT_10, new BCryptPasswordEncoder(10)),
                new PasswordEncoderRegistry.PasswordEncoderDefinition(
                        PasswordEncoderIds.BCRYPT_12, new BCryptPasswordEncoder(12)),
                new PasswordEncoderRegistry.PasswordEncoderDefinition(
                        PasswordEncoderIds.PBKDF2,
                        Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8()));
        return new PasswordEncoderRegistry(definitions);
    }

    /** Spring Security 默认 Bean；业务密码编解码请使用 {@link PasswordEncoderRegistry} */
    @Bean
    public PasswordEncoder defaultPasswordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
