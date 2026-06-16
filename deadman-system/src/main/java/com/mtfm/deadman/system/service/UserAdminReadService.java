package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtfm.deadman.common.enums.AccountType;
import com.mtfm.deadman.plugin.datascope.annotation.DataScope;
import com.mtfm.deadman.system.dto.user.UserAdminPageQuery;
import com.mtfm.deadman.system.entity.UserAccount;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.vo.user.UserAdminDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 管理端用户读服务：仅负责带数据权限的查询，不做 VO 组装。
 */
@Service
@RequiredArgsConstructor
public class UserAdminReadService {

    private final UserBaseService userBaseService;
    private final UserAccountService userAccountService;
    private final UserAdminViewAssembler viewAssembler;

    /**
     * 分页查询用户基础记录（受 {@link DataScope} 约束）。
     *
     * @param query 分页与筛选条件
     * @return 用户基础信息分页
     */
    @DataScope
    public Page<UserBase> pageUserRecords(UserAdminPageQuery query) {
        LambdaQueryWrapper<UserBase> wrapper = new LambdaQueryWrapper<UserBase>().orderByDesc(UserBase::getCreateTime);
        if (query.getStatus() != null) {
            wrapper.eq(UserBase::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            List<Long> accountMatchedIds = userAccountService.list(new LambdaQueryWrapper<UserAccount>()
                            .in(
                                    UserAccount::getAccountType,
                                    AccountType.USERNAME.getCode(),
                                    AccountType.PHONE.getCode())
                            .like(UserAccount::getAccountIdentifier, keyword))
                    .stream()
                    .map(UserAccount::getUserId)
                    .distinct()
                    .toList();
            wrapper.and(w -> {
                w.like(UserBase::getNickname, keyword).or().like(UserBase::getUserCode, keyword);
                if (!accountMatchedIds.isEmpty()) {
                    w.or().in(UserBase::getId, accountMatchedIds);
                }
            });
        }
        return userBaseService.page(new Page<>(query.resolvedCurrent(), query.resolvedSize()), wrapper);
    }

    /**
     * 查询用户详情 VO（受 {@link DataScope} 约束）。
     *
     * @param userId 用户 ID
     * @return 用户详情
     */
    @DataScope
    public UserAdminDetailVO getUserDetail(Long userId) {
        UserBase user = userBaseService.requireById(userId);
        return viewAssembler.toDetail(user);
    }
}
