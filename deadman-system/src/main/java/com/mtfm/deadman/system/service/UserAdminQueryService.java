package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtfm.deadman.common.enums.AccountType;
import com.mtfm.deadman.common.page.PageVO;
import com.mtfm.deadman.plugin.datascope.annotation.DataScope;
import com.mtfm.deadman.system.dto.user.UserAdminPageQuery;
import com.mtfm.deadman.system.entity.UserAccount;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.vo.org.OrgRefVO;
import com.mtfm.deadman.system.vo.user.UserAdminDetailVO;
import com.mtfm.deadman.system.vo.user.UserAdminSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 管理端用户读操作：分页列表与详情查询。
 */
@Service
@RequiredArgsConstructor
public class UserAdminQueryService {

    private final UserBaseService userBaseService;
    private final UserAccountService userAccountService;
    private final UserOrgService userOrgService;
    private final UserPositionService userPositionService;
    private final UserAdminViewAssembler viewAssembler;

    /**
     * 分页查询用户列表。
     *
     * @param query 分页与筛选条件
     * @return 用户分页列表
     */
    @DataScope
    public PageVO<UserAdminSummaryVO> pageUsers(UserAdminPageQuery query) {
        LambdaQueryWrapper<UserBase> wrapper = new LambdaQueryWrapper<UserBase>().orderByDesc(UserBase::getCreateTime);
        if (query.getStatus() != null) {
            wrapper.eq(UserBase::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            List<Long> accountMatchedIds = userAccountService.list(new LambdaQueryWrapper<UserAccount>()
                            .in(
                                    UserAccount::getAccountType,
                                    AccountType.USERNAME.getCode(),
                                    AccountType.PHONE.getCode())
                            .like(UserAccount::getAccountIdentifier, kw))
                    .stream()
                    .map(UserAccount::getUserId)
                    .distinct()
                    .toList();
            wrapper.and(w -> {
                w.like(UserBase::getNickname, kw).or().like(UserBase::getUserCode, kw);
                if (!accountMatchedIds.isEmpty()) {
                    w.or().in(UserBase::getId, accountMatchedIds);
                }
            });
        }

        Page<UserBase> page = userBaseService.page(
                new Page<>(query.resolvedCurrent(), query.resolvedSize()), wrapper);
        List<UserBase> records = page.getRecords();
        if (records.isEmpty()) {
            return PageVO.of(List.of(), page.getTotal(), query);
        }

        List<Long> userIds = records.stream().map(UserBase::getId).toList();
        Map<Long, String> usernames = viewAssembler.loadPrimaryUsernames(userIds);
        Map<Long, String> phones = userAccountService.loadPhonesByUserIds(userIds);
        Map<Long, List<String>> roleCodesMap = viewAssembler.loadRoleCodesByUserIds(userIds);
        Map<Long, OrgRefVO> departmentRefs = userOrgService.loadDepartmentRefs(
                records.stream().map(UserBase::getDepartmentId).toList());
        Map<Long, List<OrgRefVO>> positionsMap = userPositionService.loadPositionRefsByUserIds(userIds);

        List<UserAdminSummaryVO> items = records.stream()
                .map(user -> viewAssembler.toSummary(
                        user,
                        usernames.get(user.getId()),
                        phones.get(user.getId()),
                        departmentRefs.get(user.getDepartmentId()),
                        positionsMap.getOrDefault(user.getId(), List.of()),
                        roleCodesMap.getOrDefault(user.getId(), List.of())))
                .toList();
        return PageVO.of(items, page.getTotal(), query);
    }

    /**
     * 用户详情。
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
