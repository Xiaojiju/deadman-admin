package com.mtfm.deadman.system.domain.department;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.common.util.DedupUtils;
import com.mtfm.deadman.system.entity.SysDepartment;
import com.mtfm.deadman.system.entity.SysUserDepartment;
import com.mtfm.deadman.system.mapper.SysUserDepartmentMapper;
import com.mtfm.deadman.system.vo.org.OrgRefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 部门领域操作：以部门为主视角维护用户归属与读模型。
 */
@Service
@RequiredArgsConstructor
public class DepartmentOperations {

    private static final int PRIMARY_FLAG = 1;
    private static final int NON_PRIMARY_FLAG = 0;

    private final SysUserDepartmentMapper sysUserDepartmentMapper;
    private final DepartmentValidator departmentValidator;
    private final com.mtfm.deadman.system.service.SysDepartmentService sysDepartmentService;

    /**
     * 移除用户全部部门绑定。
     *
     * @param userId 用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAllDepartmentsForUser(Long userId) {
        sysUserDepartmentMapper.delete(
                new LambdaQueryWrapper<SysUserDepartment>().eq(SysUserDepartment::getUserId, userId));
    }

    /**
     * 覆盖式设置部门成员。
     *
     * @param deptId  部门 ID
     * @param userIds 用户 ID 列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void replaceDepartmentUsers(Long deptId, List<Long> userIds) {
        departmentValidator.requireActiveDepartment(deptId);
        List<Long> normalized = DedupUtils.dedupeLongs(userIds);
        departmentValidator.requireUsersExist(normalized);

        List<Long> currentUserIds = sysUserDepartmentMapper.selectUserIdsByDeptId(deptId);
        Set<Long> targetSet = new LinkedHashSet<>(normalized);
        Set<Long> currentSet = new LinkedHashSet<>(currentUserIds);

        for (Long userId : currentSet) {
            if (!targetSet.contains(userId)) {
                removeUserFromDepartment(deptId, userId);
            }
        }
        for (Long userId : targetSet) {
            if (!currentSet.contains(userId)) {
                addUserToDepartment(deptId, userId, false);
            }
        }
    }

    /**
     * 向部门增量添加用户。
     *
     * @param deptId  部门 ID
     * @param userIds 用户 ID 列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void addUsersToDepartment(Long deptId, List<Long> userIds) {
        departmentValidator.requireActiveDepartment(deptId);
        List<Long> normalized = DedupUtils.dedupeLongs(userIds);
        departmentValidator.requireUsersExist(normalized);
        for (Long userId : normalized) {
            addUserToDepartment(deptId, userId, false);
        }
    }

    /**
     * 从部门移除用户。
     *
     * @param deptId  部门 ID
     * @param userIds 用户 ID 列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeUsersFromDepartment(Long deptId, List<Long> userIds) {
        departmentValidator.requireActiveDepartment(deptId);
        List<Long> normalized = DedupUtils.dedupeLongs(userIds);
        for (Long userId : normalized) {
            removeUserFromDepartment(deptId, userId);
        }
    }

    /**
     * 覆盖式设置用户所属部门及主部门。
     *
     * @param userId             用户 ID
     * @param departmentIds      部门 ID 列表，空列表表示清空
     * @param primaryDepartmentId 主部门 ID，为空时取列表首项
     */
    @Transactional(rollbackFor = Exception.class)
    public void replaceUserDepartments(Long userId, List<Long> departmentIds, Long primaryDepartmentId) {
        departmentValidator.requireUser(userId);
        List<Long> normalized = DedupUtils.dedupeLongs(departmentIds);
        for (Long deptId : normalized) {
            departmentValidator.requireActiveDepartment(deptId);
        }

        sysUserDepartmentMapper.delete(
                new LambdaQueryWrapper<SysUserDepartment>().eq(SysUserDepartment::getUserId, userId));

        if (normalized.isEmpty()) {
            return;
        }

        Long primary = resolvePrimaryDepartmentId(normalized, primaryDepartmentId);
        for (Long deptId : normalized) {
            int isPrimary = deptId.equals(primary) ? PRIMARY_FLAG : NON_PRIMARY_FLAG;
            sysUserDepartmentMapper.insert(SysUserDepartment.builder()
                    .userId(userId)
                    .deptId(deptId)
                    .isPrimary(isPrimary)
                    .build());
        }
    }

    /**
     * 查询用户绑定的部门 ID 列表。
     *
     * @param userId 用户 ID
     * @return 部门 ID 列表
     */
    public List<Long> findDepartmentIdsByUser(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return sysUserDepartmentMapper.selectDeptIdsByUserId(userId);
    }

    /**
     * 查询用户主部门 ID。
     *
     * @param userId 用户 ID
     * @return 主部门 ID
     */
    public Long findPrimaryDepartmentId(Long userId) {
        if (userId == null) {
            return null;
        }
        return sysUserDepartmentMapper.selectPrimaryDeptIdByUserId(userId);
    }

    /**
     * 查询部门下的用户 ID 列表。
     *
     * @param deptId 部门 ID
     * @return 用户 ID 列表
     */
    public List<Long> findUserIdsByDepartment(Long deptId) {
        if (deptId == null) {
            return List.of();
        }
        return sysUserDepartmentMapper.selectUserIdsByDeptId(deptId);
    }

    /**
     * 判断部门下是否存在用户。
     *
     * @param deptId 部门 ID
     * @return 是否存在关联用户
     */
    public boolean hasUsersInDepartment(Long deptId) {
        if (deptId == null) {
            return false;
        }
        return sysUserDepartmentMapper.countUsersByDeptId(deptId) > 0;
    }

    /**
     * 批量加载用户的部门引用。
     *
     * @param userIds 用户 ID 列表
     * @return 用户 ID 到部门引用列表的映射
     */
    public Map<Long, List<OrgRefVO>> loadDepartmentRefsByUserIds(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<SysUserDepartment> bindings = sysUserDepartmentMapper.selectByUserIds(userIds);
        if (bindings.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> deptIds = bindings.stream().map(SysUserDepartment::getDeptId).distinct().toList();
        Map<Long, OrgRefVO> deptRefMap = loadDepartmentRefs(deptIds);
        Map<Long, List<OrgRefVO>> result = new HashMap<>();
        for (SysUserDepartment binding : bindings) {
            OrgRefVO ref = deptRefMap.get(binding.getDeptId());
            if (ref != null) {
                result.computeIfAbsent(binding.getUserId(), ignored -> new ArrayList<>()).add(ref);
            }
        }
        return result;
    }

    /**
     * 批量加载用户主部门引用。
     *
     * @param userIds 用户 ID 列表
     * @return 用户 ID 到主部门引用的映射
     */
    public Map<Long, OrgRefVO> loadPrimaryDepartmentRefsByUserIds(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<SysUserDepartment> bindings = sysUserDepartmentMapper.selectByUserIds(userIds);
        List<Long> primaryDeptIds = bindings.stream()
                .filter(binding -> binding.getIsPrimary() != null && binding.getIsPrimary() == PRIMARY_FLAG)
                .map(SysUserDepartment::getDeptId)
                .distinct()
                .toList();
        Map<Long, OrgRefVO> deptRefMap = loadDepartmentRefs(primaryDeptIds);
        Map<Long, OrgRefVO> result = new HashMap<>();
        for (SysUserDepartment binding : bindings) {
            if (binding.getIsPrimary() != null && binding.getIsPrimary() == PRIMARY_FLAG) {
                OrgRefVO ref = deptRefMap.get(binding.getDeptId());
                if (ref != null) {
                    result.put(binding.getUserId(), ref);
                }
            }
        }
        return result;
    }

    /**
     * 加载单个用户的部门引用列表。
     *
     * @param userId 用户 ID
     * @return 部门引用列表
     */
    public List<OrgRefVO> loadDepartmentRefsByUserId(Long userId) {
        return loadDepartmentRefs(findDepartmentIdsByUser(userId)).values().stream().toList();
    }

    /**
     * 加载单个用户的主部门引用。
     *
     * @param userId 用户 ID
     * @return 主部门引用，未设置时返回 null
     */
    public OrgRefVO loadPrimaryDepartmentRef(Long userId) {
        Long primaryDeptId = findPrimaryDepartmentId(userId);
        if (primaryDeptId == null) {
            return null;
        }
        return loadDepartmentRefs(List.of(primaryDeptId)).get(primaryDeptId);
    }

    /**
     * 批量加载部门引用。
     *
     * @param departmentIds 部门 ID 列表
     * @return 部门 ID 到引用的映射
     */
    public Map<Long, OrgRefVO> loadDepartmentRefs(List<Long> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ids = departmentIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysDepartmentService.listByIds(ids).stream()
                .collect(Collectors.toMap(
                        SysDepartment::getId,
                        department -> new OrgRefVO(
                                department.getId(), department.getDeptCode(), department.getDeptName())));
    }

    private void addUserToDepartment(Long deptId, Long userId, boolean asPrimary) {
        Long existing = sysUserDepartmentMapper.selectCount(new LambdaQueryWrapper<SysUserDepartment>()
                .eq(SysUserDepartment::getUserId, userId)
                .eq(SysUserDepartment::getDeptId, deptId));
        if (existing != null && existing > 0) {
            return;
        }
        if (asPrimary) {
            clearPrimaryFlag(userId);
        } else if (findPrimaryDepartmentId(userId) == null) {
            asPrimary = true;
        }
        sysUserDepartmentMapper.insert(SysUserDepartment.builder()
                .userId(userId)
                .deptId(deptId)
                .isPrimary(asPrimary ? PRIMARY_FLAG : NON_PRIMARY_FLAG)
                .build());
    }

    private void removeUserFromDepartment(Long deptId, Long userId) {
        SysUserDepartment binding = sysUserDepartmentMapper.selectOne(new LambdaQueryWrapper<SysUserDepartment>()
                .eq(SysUserDepartment::getUserId, userId)
                .eq(SysUserDepartment::getDeptId, deptId));
        if (binding == null) {
            return;
        }
        boolean wasPrimary = binding.getIsPrimary() != null && binding.getIsPrimary() == PRIMARY_FLAG;
        sysUserDepartmentMapper.deleteById(binding.getId());
        if (wasPrimary) {
            promoteNextPrimaryDepartment(userId);
        }
    }

    private void promoteNextPrimaryDepartment(Long userId) {
        List<Long> remaining = findDepartmentIdsByUser(userId);
        if (remaining.isEmpty()) {
            return;
        }
        SysUserDepartment next = sysUserDepartmentMapper.selectOne(new LambdaQueryWrapper<SysUserDepartment>()
                .eq(SysUserDepartment::getUserId, userId)
                .eq(SysUserDepartment::getDeptId, remaining.get(0)));
        if (next != null) {
            next.setIsPrimary(PRIMARY_FLAG);
            sysUserDepartmentMapper.updateById(next);
        }
    }

    private void clearPrimaryFlag(Long userId) {
        List<SysUserDepartment> bindings = sysUserDepartmentMapper.selectList(new LambdaQueryWrapper<SysUserDepartment>()
                .eq(SysUserDepartment::getUserId, userId)
                .eq(SysUserDepartment::getIsPrimary, PRIMARY_FLAG));
        for (SysUserDepartment binding : bindings) {
            binding.setIsPrimary(NON_PRIMARY_FLAG);
            sysUserDepartmentMapper.updateById(binding);
        }
    }

    private Long resolvePrimaryDepartmentId(List<Long> departmentIds, Long primaryDepartmentId) {
        if (departmentIds.isEmpty()) {
            return null;
        }
        Long primary = primaryDepartmentId != null ? primaryDepartmentId : departmentIds.get(0);
        if (!departmentIds.contains(primary)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "主部门须包含在部门列表中");
        }
        return primary;
    }
}
