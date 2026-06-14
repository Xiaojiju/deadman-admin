package com.mtfm.deadman.plugin.datascope.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.datascope.entity.UserDataScope;
import com.mtfm.deadman.plugin.datascope.entity.UserDataScopeDept;
import com.mtfm.deadman.plugin.datascope.mapper.UserDataScopeDeptMapper;
import com.mtfm.deadman.plugin.datascope.mapper.UserDataScopeMapper;
import com.mtfm.deadman.plugin.datascope.model.DataScopeProfile;
import com.mtfm.deadman.plugin.datascope.model.DataScopeType;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户数据权限持久化配置读写。
 */
@Service
public class UserDataScopeProfileService extends ServiceImpl<UserDataScopeMapper, UserDataScope> {

    private final UserDataScopeDeptMapper userDataScopeDeptMapper;
    private final DataScopeSessionCache sessionCache;

    /**
     * @param userDataScopeDeptMapper CUSTOM 可见部门 Mapper
     * @param sessionCache            运行时缓存（延迟注入，避免与 ContextResolver 形成构造期环）
     */
    public UserDataScopeProfileService(
            UserDataScopeDeptMapper userDataScopeDeptMapper, @Lazy DataScopeSessionCache sessionCache) {
        this.userDataScopeDeptMapper = userDataScopeDeptMapper;
        this.sessionCache = sessionCache;
    }

    /**
     * 解析用户数据权限配置，未配置时返回默认本部门策略。
     *
     * @param userId 用户 ID
     * @return 数据权限配置
     */
    public DataScopeProfile resolveProfile(Long userId) {
        if (userId == null) {
            return DataScopeProfile.defaultProfile();
        }
        UserDataScope scope = getOne(new LambdaQueryWrapper<UserDataScope>().eq(UserDataScope::getUserId, userId));
        if (scope == null || !StringUtils.hasText(scope.getScopeType())) {
            return DataScopeProfile.defaultProfile();
        }
        DataScopeType scopeType = DataScopeType.fromCode(scope.getScopeType());
        if (scopeType == null) {
            return DataScopeProfile.defaultProfile();
        }
        Set<Long> customDeptIds = scopeType == DataScopeType.CUSTOM ? loadCustomDeptIds(userId) : Set.of();
        return new DataScopeProfile(scopeType, customDeptIds);
    }

    /**
     * 为新用户写入默认数据范围（本部门）。
     *
     * @param userId 用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignDefaultScope(Long userId) {
        assignScope(userId, DataScopeProfile.DEFAULT_SCOPE_TYPE, Collections.emptyList());
    }

    /**
     * 分配或更新用户数据范围。
     *
     * @param userId        用户 ID
     * @param scopeType     数据范围类型
     * @param customDeptIds CUSTOM 范围下的可见部门 ID 列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignScope(Long userId, DataScopeType scopeType, List<Long> customDeptIds) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户 ID 不能为空");
        }
        if (scopeType == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "数据范围类型不能为空");
        }
        if (scopeType == DataScopeType.CUSTOM) {
            validateCustomDeptIds(customDeptIds);
        }
        UserDataScope existing = getOne(new LambdaQueryWrapper<UserDataScope>().eq(UserDataScope::getUserId, userId));
        if (existing == null) {
            save(UserDataScope.builder().userId(userId).scopeType(scopeType.name()).build());
        } else {
            existing.setScopeType(scopeType.name());
            updateById(existing);
        }
        replaceCustomDeptIds(userId, scopeType, customDeptIds);
        sessionCache.refresh(userId);
    }

    /**
     * 删除用户的数据权限配置。
     *
     * @param userId 用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeByUserId(Long userId) {
        remove(new LambdaQueryWrapper<UserDataScope>().eq(UserDataScope::getUserId, userId));
        userDataScopeDeptMapper.delete(
                new LambdaQueryWrapper<UserDataScopeDept>().eq(UserDataScopeDept::getUserId, userId));
        sessionCache.evict(userId);
    }

    private Set<Long> loadCustomDeptIds(Long userId) {
        return userDataScopeDeptMapper.selectList(new LambdaQueryWrapper<UserDataScopeDept>()
                .eq(UserDataScopeDept::getUserId, userId))
                .stream()
                .map(UserDataScopeDept::getDeptId)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    private void replaceCustomDeptIds(Long userId, DataScopeType scopeType, List<Long> customDeptIds) {
        userDataScopeDeptMapper.delete(
                new LambdaQueryWrapper<UserDataScopeDept>().eq(UserDataScopeDept::getUserId, userId));
        if (scopeType != DataScopeType.CUSTOM || customDeptIds == null || customDeptIds.isEmpty()) {
            return;
        }
        Set<Long> uniqueIds = new LinkedHashSet<>(customDeptIds);
        for (Long deptId : uniqueIds) {
            userDataScopeDeptMapper.insert(UserDataScopeDept.builder().userId(userId).deptId(deptId).build());
        }
    }

    private void validateCustomDeptIds(List<Long> customDeptIds) {
        if (customDeptIds == null || customDeptIds.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "CUSTOM 数据范围必须指定可见部门");
        }
    }
}
