package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtfm.deadman.common.enums.AccountType;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.system.entity.UserAccount;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.system.mapper.UserAccountMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户登录账号服务：用户名、手机、OAuth 等通用查询。
 */
@Service
public class UserAccountService extends ServiceImpl<UserAccountMapper, UserAccount> {

    /**
     * 按用户名查询账号（USERNAME 类型）。
     * 
     * @param username 用户名
     * @return 用户账号
     */
    public UserAccount findByUsername(String username) {
        return findByTypeAndIdentifier(AccountType.USERNAME.getCode(), username, null);
    }

    /**
     * 按手机号查询账号（PHONE 类型）。
     * 
     * @param phone 手机号
     * @return 用户账号
     */
    public UserAccount findByPhone(String phone) {
        return findByTypeAndIdentifier(AccountType.PHONE.getCode(), phone, null);
    }

    /**
     * 按 OAuth 提供商与 subject 查询账号。
     * 
     * @param provider OAuth 提供商
     * @param subject  subject
     * @return 用户账号
     */
    public UserAccount findByOAuth(String provider, String subject) {
        return findByTypeAndIdentifier(AccountType.OAUTH.getCode(), subject, provider);
    }

    /**
     * 判断用户名是否已注册。
     * 
     * @param username 用户名
     * @return 是否已注册
     */
    public boolean existsUsername(String username) {
        return count(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getAccountType, AccountType.USERNAME.getCode())
                .eq(UserAccount::getAccountIdentifier, username)) > 0;
    }

    /**
     * 查询用户绑定的手机号（PHONE 类型账号标识）。
     *
     * @param userId 用户 ID
     * @return 手机号，未绑定时返回 null
     */
    public String findPhoneByUserId(Long userId) {
        UserAccount account = getOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUserId, userId)
                .eq(UserAccount::getAccountType, AccountType.PHONE.getCode()));
        return account == null ? null : account.getAccountIdentifier();
    }

    /**
     * 判断手机号是否已被其他用户占用。
     *
     * @param phone         手机号
     * @param excludeUserId 排除的用户 ID（更新本人手机时使用）
     * @return 是否已被占用
     */
    public boolean existsPhone(String phone, Long excludeUserId) {
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getAccountType, AccountType.PHONE.getCode())
                .eq(UserAccount::getAccountIdentifier, phone);
        if (excludeUserId != null) {
            wrapper.ne(UserAccount::getUserId, excludeUserId);
        }
        return count(wrapper) > 0;
    }

    /**
     * 绑定或更新用户手机号；phone 为空时不处理。
     *
     * @param userId 用户 ID
     * @param phone  手机号
     */
    public void bindOrUpdatePhone(Long userId, String phone) {
        if (!StringUtils.hasText(phone)) {
            return;
        }
        if (existsPhone(phone, userId)) {
            throw new BusinessException(ResultCode.PHONE_EXISTS);
        }

        UserAccount existing = getOne(new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getUserId, userId)
                .eq(UserAccount::getAccountType, AccountType.PHONE.getCode()));
        if (existing == null) {
            UserAccount account = UserAccount.builder()
                    .userId(userId)
                    .accountType(AccountType.PHONE.getCode())
                    .accountIdentifier(phone)
                    .verified(1)
                    .status(UserStatus.ACTIVE.getValue())
                    .build();
            save(account);
            return;
        }
        existing.setAccountIdentifier(phone);
        existing.setVerified(1);
        updateById(existing);
    }

    /**
     * 将 OAuth 账号绑定到指定用户；若 openid 已被其他用户占用则抛出业务异常。
     *
     * @param userId   用户 ID
     * @param provider OAuth 提供商标识
     * @param subject  OAuth 用户唯一标识（如 openid）
     */
    public void bindOAuth(Long userId, String provider, String subject) {
        if (!StringUtils.hasText(provider) || !StringUtils.hasText(subject)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "OAuth 绑定参数不完整");
        }
        UserAccount existing = findByOAuth(provider, subject);
        if (existing != null) {
            if (!existing.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.OAUTH_ALREADY_BOUND);
            }
            return;
        }
        UserAccount account = UserAccount.builder()
                .userId(userId)
                .accountType(AccountType.OAUTH.getCode())
                .accountIdentifier(subject)
                .oauthProvider(provider)
                .oauthSubject(subject)
                .verified(1)
                .status(UserStatus.ACTIVE.getValue())
                .build();
        save(account);
    }

    /**
     * 批量加载用户手机号。
     *
     * @param userIds 用户 ID 列表
     * @return 用户 ID 到手机号的映射
     */
    public Map<Long, String> loadPhonesByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return list(new LambdaQueryWrapper<UserAccount>()
                        .in(UserAccount::getUserId, userIds)
                        .eq(UserAccount::getAccountType, AccountType.PHONE.getCode()))
                .stream()
                .collect(Collectors.toMap(UserAccount::getUserId, UserAccount::getAccountIdentifier, (a, b) -> a));
    }

    /**
     * 根据账号类型和标识符获取用户账号
     * 
     * @param accountType   账号类型
     * @param identifier    标识符
     * @param oauthProvider OAuth 提供商
     * @return 用户账号
     */
    private UserAccount findByTypeAndIdentifier(String accountType, String identifier, String oauthProvider) {
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getAccountType, accountType)
                .eq(UserAccount::getAccountIdentifier, identifier);
        if (oauthProvider != null) {
            wrapper.eq(UserAccount::getOauthProvider, oauthProvider);
        } else {
            wrapper.isNull(UserAccount::getOauthProvider);
        }
        UserAccount account = getOne(wrapper);
        if (account == null) {
            return null;
        }
        if (account.getStatus() != null && account.getStatus() == UserStatus.DISABLED.getValue()) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        return account;
    }
}
