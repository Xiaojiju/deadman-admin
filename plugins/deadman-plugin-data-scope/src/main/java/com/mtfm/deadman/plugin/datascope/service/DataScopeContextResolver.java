package com.mtfm.deadman.plugin.datascope.service;

import com.mtfm.deadman.common.spi.DataScopeDepartmentTreeBridge;
import com.mtfm.deadman.common.spi.DataScopeUserBridge;
import com.mtfm.deadman.plugin.datascope.model.DataScopeProfile;
import com.mtfm.deadman.plugin.datascope.model.DataScopeUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * 将持久化数据权限配置与用户部门信息解析为运行时上下文。
 */
@Service
@RequiredArgsConstructor
public class DataScopeContextResolver {

    private final UserDataScopeProfileService profileService;
    private final DataScopeUserBridge userBridge;
    private final DataScopeDepartmentTreeBridge departmentTreeBridge;

    /**
     * 构建用户数据权限运行时上下文。
     *
     * @param userId 用户 ID
     * @return 运行时上下文
     */
    public DataScopeUserContext resolve(Long userId) {
        DataScopeProfile profile = profileService.resolveProfile(userId);
        Long departmentId = userBridge.findDepartmentId(userId);
        Set<Long> visibleDeptIds = resolveVisibleDeptIds(profile, departmentId);
        return new DataScopeUserContext(userId, departmentId, profile.scopeType(), visibleDeptIds, false);
    }

    private Set<Long> resolveVisibleDeptIds(DataScopeProfile profile, Long departmentId) {
        return switch (profile.scopeType()) {
            case ALL, SELF -> Set.of();
            case DEPT -> departmentId == null ? Set.of() : Set.of(departmentId);
            case DEPT_AND_CHILD -> departmentId == null
                    ? Set.of()
                    : departmentTreeBridge.resolveSelfAndDescendantIds(departmentId);
            case CUSTOM -> profile.customDeptIds().isEmpty()
                    ? Set.of()
                    : new HashSet<>(profile.customDeptIds());
        };
    }
}
