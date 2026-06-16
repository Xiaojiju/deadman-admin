package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtfm.deadman.system.entity.UserPassword;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.system.mapper.UserPasswordMapper;
import com.mtfm.deadman.core.password.PasswordEncoderRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户密码持久化与校验，编码器由
 * {@link com.mtfm.deadman.core.password.PasswordEncoderRegistry} 按
 * encoder_id 选择。
 */
@Service
@RequiredArgsConstructor
public class UserPasswordService extends ServiceImpl<UserPasswordMapper, UserPassword> {

    private final PasswordEncoderRegistry passwordEncoderRegistry;

    /**
     * 为新用户创建唯一密码记录，随机选取 PasswordEncoder。
     * 
     * @param userId      用户ID
     * @param rawPassword 原始密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void createPassword(Long userId, String rawPassword) {
        PasswordEncoderRegistry.EncodedPassword encoded = passwordEncoderRegistry.encodeWithRandomEncoder(rawPassword);
        UserPassword entity = UserPassword.builder()
                .userId(userId)
                .passwordHash(encoded.hash())
                .encoderId(encoded.encoderId())
                .passwordVersion(1)
                .build();
        save(entity);
    }

    /**
     * 校验明文密码是否与库中哈希匹配。
     * 
     * @param userId      用户ID
     * @param rawPassword 原始密码
     * @return 是否匹配
     */
    public boolean matches(Long userId, String rawPassword) {
        UserPassword stored = getByUserId(userId);
        if (stored == null) {
            throw new BusinessException(ResultCode.PASSWORD_NOT_SET);
        }
        return passwordEncoderRegistry.matches(stored.getEncoderId(), rawPassword, stored.getPasswordHash());
    }

    /**
     * 修改密码：校验原密码后重新随机选取编码器并递增 password_version。
     * 
     * @param userId      用户ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (!matches(userId, oldPassword)) {
            throw new BusinessException(ResultCode.PASSWORD_MISMATCH, "原密码错误");
        }
        UserPassword stored = getByUserId(userId);
        PasswordEncoderRegistry.EncodedPassword encoded = passwordEncoderRegistry.encodeWithRandomEncoder(newPassword);
        stored.setPasswordHash(encoded.hash());
        stored.setEncoderId(encoded.encoderId());
        stored.setPasswordVersion(stored.getPasswordVersion() + 1);
        updateById(stored);
    }

    /**
     * 管理端重置密码：不校验原密码，重新随机选取编码器并递增 password_version。
     *
     * @param userId      用户 ID
     * @param newPassword 新密码明文
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword) {
        UserPassword stored = getByUserId(userId);
        PasswordEncoderRegistry.EncodedPassword encoded = passwordEncoderRegistry.encodeWithRandomEncoder(newPassword);
        if (stored == null) {
            save(UserPassword.builder()
                    .userId(userId)
                    .passwordHash(encoded.hash())
                    .encoderId(encoded.encoderId())
                    .passwordVersion(1)
                    .build());
            return;
        }
        stored.setPasswordHash(encoded.hash());
        stored.setEncoderId(encoded.encoderId());
        stored.setPasswordVersion(stored.getPasswordVersion() + 1);
        updateById(stored);
    }

    /**
     * 根据用户ID获取用户密码
     * 
     * @param userId 用户ID
     * @return 用户密码
     */
    public UserPassword getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<UserPassword>().eq(UserPassword::getUserId, userId));
    }

    /**
     * 删除用户密码记录。
     *
     * @param userId 用户 ID
     */
    public void removeByUserId(Long userId) {
        remove(new LambdaQueryWrapper<UserPassword>().eq(UserPassword::getUserId, userId));
    }
}
