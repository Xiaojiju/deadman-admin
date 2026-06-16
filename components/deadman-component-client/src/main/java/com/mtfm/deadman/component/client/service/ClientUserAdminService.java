package com.mtfm.deadman.component.client.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtfm.deadman.common.enums.AccountType;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.page.PageVO;
import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.component.client.dto.ClientUserAdminPageQuery;
import com.mtfm.deadman.component.client.entity.ClientUserAccount;
import com.mtfm.deadman.component.client.entity.ClientUserBase;
import com.mtfm.deadman.component.client.entity.ClientUserPassword;
import com.mtfm.deadman.component.client.vo.ClientUserAccountBindingVO;
import com.mtfm.deadman.component.client.vo.ClientUserAdminDetailVO;
import com.mtfm.deadman.component.client.vo.ClientUserAdminSummaryVO;
import com.mtfm.deadman.security.token.AuthTokenIssueProviderRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端对用户端用户的分页、详情、禁用与注销。
 */
@Service
@RequiredArgsConstructor
public class ClientUserAdminService {

    private final ClientUserService clientUserService;
    private final ClientUserAccountService clientUserAccountService;
    private final ClientUserPasswordService clientUserPasswordService;
    private final AuthTokenIssueProviderRegistry providerRegistry;

    /**
     * 分页查询用户端用户。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    public PageVO<ClientUserAdminSummaryVO> pageUsers(ClientUserAdminPageQuery query) {
        LambdaQueryWrapper<ClientUserBase> wrapper = new LambdaQueryWrapper<ClientUserBase>()
                .orderByDesc(ClientUserBase::getCreateTime);
        if (query.getStatus() != null) {
            wrapper.eq(ClientUserBase::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            List<Long> accountMatchedIds = clientUserAccountService
                    .list(new LambdaQueryWrapper<ClientUserAccount>()
                            .in(
                                    ClientUserAccount::getAccountType,
                                    AccountType.USERNAME.getCode(),
                                    AccountType.PHONE.getCode())
                            .like(ClientUserAccount::getAccountIdentifier, keyword))
                    .stream()
                    .map(ClientUserAccount::getUserId)
                    .distinct()
                    .toList();
            wrapper.and(w -> {
                w.like(ClientUserBase::getNickname, keyword).or().like(ClientUserBase::getUserCode, keyword);
                if (!accountMatchedIds.isEmpty()) {
                    w.or().in(ClientUserBase::getId, accountMatchedIds);
                }
            });
        }

        Page<ClientUserBase> page = clientUserService.page(
                new Page<>(query.resolvedCurrent(), query.resolvedSize()), wrapper);
        List<ClientUserBase> records = page.getRecords();
        if (records.isEmpty()) {
            return PageVO.of(List.of(), page.getTotal(), query);
        }

        List<Long> userIds = records.stream().map(ClientUserBase::getId).toList();
        Map<Long, String> usernames = loadPrimaryUsernames(userIds);
        Map<Long, String> phones = clientUserAccountService.loadPhonesByUserIds(userIds);

        List<ClientUserAdminSummaryVO> items = records.stream()
                .map(user -> new ClientUserAdminSummaryVO(
                        user.getId(),
                        user.getUserCode(),
                        usernames.get(user.getId()),
                        user.getNickname(),
                        user.getAvatar(),
                        phones.get(user.getId()),
                        user.getStatus(),
                        user.getCreateTime()))
                .toList();
        return PageVO.of(items, page.getTotal(), query);
    }

    /**
     * 用户详情。
     *
     * @param userId 用户 ID
     * @return 详情
     */
    public ClientUserAdminDetailVO getUserDetail(Long userId) {
        ClientUserBase user = clientUserService.requireById(userId);
        return toDetail(user);
    }

    /**
     * 禁用用户端用户。
     *
     * @param userId 用户 ID
     * @return 更新后详情
     */
    @Transactional(rollbackFor = Exception.class)
    public ClientUserAdminDetailVO disableUser(Long userId) {
        ClientUserBase user = clientUserService.requireById(userId);
        if (user.getStatus() != null && user.getStatus() == UserStatus.DISABLED.getValue()) {
            return toDetail(user);
        }
        user.setStatus(UserStatus.DISABLED.getValue());
        clientUserService.updateById(user);
        syncAccountStatus(userId, UserStatus.DISABLED.getValue());
        invalidateSessions(userId);
        return toDetail(user);
    }

    /**
     * 注销（逻辑删除）用户端用户。
     *
     * @param userId 用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        clientUserAccountService.remove(
                new LambdaQueryWrapper<ClientUserAccount>().eq(ClientUserAccount::getUserId, userId));
        clientUserPasswordService.remove(
                new LambdaQueryWrapper<ClientUserPassword>().eq(ClientUserPassword::getUserId, userId));
        clientUserService.removeById(userId);
        invalidateSessions(userId);
    }

    private ClientUserAdminDetailVO toDetail(ClientUserBase user) {
        String username = loadPrimaryUsernames(List.of(user.getId())).get(user.getId());
        String phone = clientUserAccountService.findPhoneByUserId(user.getId());
        List<ClientUserAccountBindingVO> accounts = clientUserAccountService.list(
                new LambdaQueryWrapper<ClientUserAccount>().eq(ClientUserAccount::getUserId, user.getId()))
                .stream()
                .map(account -> new ClientUserAccountBindingVO(
                        account.getAccountType(),
                        account.getAccountIdentifier(),
                        account.getOauthProvider(),
                        account.getVerified(),
                        account.getStatus()))
                .toList();
        return new ClientUserAdminDetailVO(
                user.getId(),
                user.getUserCode(),
                username,
                user.getNickname(),
                user.getAvatar(),
                phone,
                user.getStatus(),
                accounts,
                user.getCreateTime(),
                user.getUpdateTime());
    }

    private void syncAccountStatus(Long userId, Integer status) {
        List<ClientUserAccount> accounts = clientUserAccountService.list(
                new LambdaQueryWrapper<ClientUserAccount>().eq(ClientUserAccount::getUserId, userId));
        for (ClientUserAccount account : accounts) {
            account.setStatus(status);
        }
        if (!accounts.isEmpty()) {
            clientUserAccountService.updateBatchById(accounts);
        }
    }

    private void invalidateSessions(Long userId) {
        providerRegistry.require(ClientAuthConstants.JWT_REALM).invalidateUserSessions(userId);
    }

    private Map<Long, String> loadPrimaryUsernames(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return clientUserAccountService
                .list(new LambdaQueryWrapper<ClientUserAccount>()
                        .in(ClientUserAccount::getUserId, userIds)
                        .eq(ClientUserAccount::getAccountType, AccountType.USERNAME.getCode()))
                .stream()
                .collect(Collectors.toMap(
                        ClientUserAccount::getUserId, ClientUserAccount::getAccountIdentifier, (a, b) -> a));
    }
}
