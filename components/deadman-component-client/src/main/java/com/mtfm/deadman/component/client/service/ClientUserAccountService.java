package com.mtfm.deadman.component.client.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtfm.deadman.common.enums.AccountType;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.client.entity.ClientUserAccount;
import com.mtfm.deadman.component.client.mapper.ClientUserAccountMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户端登录账号服务。
 */
@Service
public class ClientUserAccountService extends ServiceImpl<ClientUserAccountMapper, ClientUserAccount> {

    /**
     * 按用户名查询账号。
     *
     * @param username 用户名
     * @return 账号，不存在时返回 null
     */
    public ClientUserAccount findByUsername(String username) {
        return findByTypeAndIdentifier(AccountType.USERNAME.getCode(), username, null);
    }

    /**
     * 判断用户名是否已注册。
     *
     * @param username 用户名
     * @return 是否已存在
     */
    public boolean existsUsername(String username) {
        return count(new LambdaQueryWrapper<ClientUserAccount>()
                .eq(ClientUserAccount::getAccountType, AccountType.USERNAME.getCode())
                .eq(ClientUserAccount::getAccountIdentifier, username)) > 0;
    }

    /**
     * 按 OAuth 提供商与 subject 查询账号。
     *
     * @param provider OAuth 提供商
     * @param subject  subject
     * @return 账号
     */
    public ClientUserAccount findByOAuth(String provider, String subject) {
        return findByTypeAndIdentifier(AccountType.OAUTH.getCode(), subject, provider);
    }

    /**
     * 查询用户绑定的手机号。
     *
     * @param userId 用户 ID
     * @return 手机号，未绑定时为 null
     */
    public String findPhoneByUserId(Long userId) {
        ClientUserAccount account = getOne(new LambdaQueryWrapper<ClientUserAccount>()
                .eq(ClientUserAccount::getUserId, userId)
                .eq(ClientUserAccount::getAccountType, AccountType.PHONE.getCode()));
        return account == null ? null : account.getAccountIdentifier();
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
        return list(new LambdaQueryWrapper<ClientUserAccount>()
                        .in(ClientUserAccount::getUserId, userIds)
                        .eq(ClientUserAccount::getAccountType, AccountType.PHONE.getCode()))
                .stream()
                .collect(Collectors.toMap(
                        ClientUserAccount::getUserId, ClientUserAccount::getAccountIdentifier, (a, b) -> a));
    }

    /**
     * 判断手机号是否已被其他用户占用。
     *
     * @param phone         手机号
     * @param excludeUserId 排除的用户 ID
     * @return 是否已占用
     */
    public boolean existsPhone(String phone, Long excludeUserId) {
        LambdaQueryWrapper<ClientUserAccount> wrapper = new LambdaQueryWrapper<ClientUserAccount>()
                .eq(ClientUserAccount::getAccountType, AccountType.PHONE.getCode())
                .eq(ClientUserAccount::getAccountIdentifier, phone);
        if (excludeUserId != null) {
            wrapper.ne(ClientUserAccount::getUserId, excludeUserId);
        }
        return count(wrapper) > 0;
    }

    /**
     * 绑定或更新用户手机号。
     *
     * @param userId 用户 ID
     * @param phone  手机号
     */
    public void bindOrUpdatePhone(Long userId, String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "手机号不能为空");
        }
        if (existsPhone(phone, userId)) {
            throw new BusinessException(ResultCode.PHONE_EXISTS);
        }
        ClientUserAccount existing = getOne(new LambdaQueryWrapper<ClientUserAccount>()
                .eq(ClientUserAccount::getUserId, userId)
                .eq(ClientUserAccount::getAccountType, AccountType.PHONE.getCode()));
        if (existing == null) {
            save(ClientUserAccount.builder()
                    .userId(userId)
                    .accountType(AccountType.PHONE.getCode())
                    .accountIdentifier(phone)
                    .verified(1)
                    .status(UserStatus.ACTIVE.getValue())
                    .build());
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
        ClientUserAccount existing = findByOAuth(provider, subject);
        if (existing != null) {
            if (!existing.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.OAUTH_ALREADY_BOUND);
            }
            return;
        }
        save(ClientUserAccount.builder()
                .userId(userId)
                .accountType(AccountType.OAUTH.getCode())
                .accountIdentifier(subject)
                .oauthProvider(provider)
                .oauthSubject(subject)
                .verified(1)
                .status(UserStatus.ACTIVE.getValue())
                .build());
    }

    private ClientUserAccount findByTypeAndIdentifier(String accountType, String identifier, String oauthProvider) {
        LambdaQueryWrapper<ClientUserAccount> wrapper = new LambdaQueryWrapper<ClientUserAccount>()
                .eq(ClientUserAccount::getAccountType, accountType)
                .eq(ClientUserAccount::getAccountIdentifier, identifier);
        if (oauthProvider != null) {
            wrapper.eq(ClientUserAccount::getOauthProvider, oauthProvider);
        } else {
            wrapper.isNull(ClientUserAccount::getOauthProvider);
        }
        ClientUserAccount account = getOne(wrapper);
        if (account == null) {
            return null;
        }
        if (account.getStatus() != null && account.getStatus() == UserStatus.DISABLED.getValue()) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        return account;
    }
}
