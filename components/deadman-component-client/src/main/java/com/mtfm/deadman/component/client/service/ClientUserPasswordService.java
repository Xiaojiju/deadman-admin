package com.mtfm.deadman.component.client.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.client.entity.ClientUserPassword;
import com.mtfm.deadman.component.client.mapper.ClientUserPasswordMapper;
import com.mtfm.deadman.core.password.PasswordEncoderRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户端密码持久化与校验。
 */
@Service
@RequiredArgsConstructor
public class ClientUserPasswordService extends ServiceImpl<ClientUserPasswordMapper, ClientUserPassword> {

    private final PasswordEncoderRegistry passwordEncoderRegistry;

    /**
     * 为新用户创建密码记录。
     *
     * @param userId      用户 ID
     * @param rawPassword 明文密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void createPassword(Long userId, String rawPassword) {
        PasswordEncoderRegistry.EncodedPassword encoded = passwordEncoderRegistry.encodeWithRandomEncoder(rawPassword);
        save(ClientUserPassword.builder()
                .userId(userId)
                .passwordHash(encoded.hash())
                .encoderId(encoded.encoderId())
                .passwordVersion(1)
                .build());
    }

    /**
     * 校验明文密码是否匹配。
     *
     * @param userId      用户 ID
     * @param rawPassword 明文密码
     * @return 是否匹配
     */
    public boolean matches(Long userId, String rawPassword) {
        ClientUserPassword stored = getByUserId(userId);
        if (stored == null) {
            throw new BusinessException(ResultCode.PASSWORD_NOT_SET);
        }
        return passwordEncoderRegistry.matches(stored.getEncoderId(), rawPassword, stored.getPasswordHash());
    }

    /**
     * 按用户 ID 查询密码记录。
     *
     * @param userId 用户 ID
     * @return 密码记录
     */
    public ClientUserPassword getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<ClientUserPassword>().eq(ClientUserPassword::getUserId, userId));
    }
}
