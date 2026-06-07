package com.mtfm.deadman.core.password;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 可插拔的 PasswordEncoder 注册表
 * <p>
 * 注册/改密时 {@link #encodeWithRandomEncoder(String)} 随机选取编码器；登录时按库中 encoder_id 校验。
 */
public class PasswordEncoderRegistry {

    private final Map<String, PasswordEncoder> encoders;

    public PasswordEncoderRegistry(List<PasswordEncoderDefinition> definitions) {
        this.encoders = definitions.stream()
                .collect(Collectors.toUnmodifiableMap(
                        PasswordEncoderDefinition::id,
                        PasswordEncoderDefinition::encoder));
    }

    /**
     * 获取注册的 PasswordEncoder
     * 
     * @param encoderId 编码器ID
     * @return PasswordEncoder
     * @throws IllegalArgumentException 如果编码器未注册
     */
    public PasswordEncoder require(String encoderId) {
        return Optional.ofNullable(encoders.get(encoderId))
                .orElseThrow(() -> new IllegalArgumentException("未注册的 PasswordEncoder: " + encoderId));
    }

    /**
     * 获取所有可用的编码器ID
     * 
     * @return 编码器ID列表
     */
    public Collection<String> availableEncoderIds() {
        return encoders.keySet();
    }

    /**
     * 随机选取一个编码器ID
     * 
     * @return 编码器ID
     */
    public String pickRandomEncoderId() {
        List<String> ids = List.copyOf(encoders.keySet());
        return ids.get(ThreadLocalRandom.current().nextInt(ids.size()));
    }

    /**
     * 编码密码
     * 
     * @param rawPassword 原始密码
     * @return 编码后的密码
     */
    public String encode(String rawPassword) {
        String encoderId = pickRandomEncoderId();
        return encode(encoderId, rawPassword);
    }

    /**
     * 随机选取一个编码器编码密码
     * 
     * @param rawPassword 原始密码
     * @return 编码后的密码
     */
    public EncodedPassword encodeWithRandomEncoder(String rawPassword) {
        String encoderId = pickRandomEncoderId();
        return new EncodedPassword(encoderId, encode(encoderId, rawPassword));
    }

    /**
     * 编码密码
     * 
     * @param encoderId   编码器ID
     * @param rawPassword 原始密码
     * @return 编码后的密码
     */
    public String encode(String encoderId, String rawPassword) {
        return require(encoderId).encode(rawPassword);
    }

    /**
     * 校验密码
     * 
     * @param encoderId       编码器ID
     * @param rawPassword     原始密码
     * @param encodedPassword 编码后的密码
     * @return 是否匹配
     */
    public boolean matches(String encoderId, String rawPassword, String encodedPassword) {
        return require(encoderId).matches(rawPassword, encodedPassword);
    }

    /**
     * 编码后的密码
     * 
     * @param encoderId 编码器ID
     * @param hash      哈希值
     */
    public record EncodedPassword(String encoderId, String hash) {
    }

    /**
     * 密码编码器定义
     * 
     * @param id      编码器ID
     * @param encoder 密码编码器
     */
    public record PasswordEncoderDefinition(String id, PasswordEncoder encoder) {
    }
}
