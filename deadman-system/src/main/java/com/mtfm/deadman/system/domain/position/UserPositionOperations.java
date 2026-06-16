package com.mtfm.deadman.system.domain.position;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.common.util.DedupUtils;
import com.mtfm.deadman.system.domain.department.DepartmentOperations;
import com.mtfm.deadman.system.domain.department.DepartmentValidator;
import com.mtfm.deadman.system.dto.org.UserPositionBindingRequest;
import com.mtfm.deadman.system.entity.SysPosition;
import com.mtfm.deadman.system.entity.SysUserPosition;
import com.mtfm.deadman.system.mapper.SysUserPositionMapper;
import com.mtfm.deadman.system.service.SysPositionService;
import com.mtfm.deadman.system.vo.org.OrgRefVO;
import com.mtfm.deadman.system.vo.org.UserPositionBindingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户职位领域操作：维护用户在已绑定部门下的职位。
 */
@Service
@RequiredArgsConstructor
public class UserPositionOperations {

    private final SysUserPositionMapper sysUserPositionMapper;
    private final SysPositionService sysPositionService;
    private final DepartmentOperations departmentOperations;
    private final DepartmentValidator departmentValidator;

    /**
     * 覆盖式绑定用户在各部门下的职位。
     *
     * @param userId   用户 ID
     * @param bindings 职位绑定列表，空列表表示清空
     */
    @Transactional(rollbackFor = Exception.class)
    public void replaceUserPositionBindings(Long userId, List<UserPositionBindingRequest> bindings) {
        departmentValidator.requireUser(userId);
        List<UserPositionBindingRequest> normalized = normalizeBindings(bindings);
        validateBindingsForUser(userId, normalized);

        sysUserPositionMapper.delete(new LambdaQueryWrapper<SysUserPosition>().eq(SysUserPosition::getUserId, userId));
        for (UserPositionBindingRequest binding : normalized) {
            sysUserPositionMapper.insert(SysUserPosition.builder()
                    .userId(userId)
                    .departmentId(binding.departmentId())
                    .positionId(binding.positionId())
                    .build());
        }
    }

    /**
     * 查询用户在各部门下的职位绑定。
     *
     * @param userId 用户 ID
     * @return 职位绑定展示列表
     */
    public List<UserPositionBindingVO> loadPositionBindingsByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<SysUserPosition> bindings = sysUserPositionMapper.selectList(new LambdaQueryWrapper<SysUserPosition>()
                .eq(SysUserPosition::getUserId, userId)
                .orderByAsc(SysUserPosition::getCreateTime));
        return toBindingVOs(bindings);
    }

    /**
     * 批量加载用户职位绑定。
     *
     * @param userIds 用户 ID 列表
     * @return 用户 ID 到职位绑定列表的映射
     */
    public Map<Long, List<UserPositionBindingVO>> loadPositionBindingsByUserIds(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<SysUserPosition> bindings = sysUserPositionMapper.selectList(new LambdaQueryWrapper<SysUserPosition>()
                .in(SysUserPosition::getUserId, userIds)
                .orderByAsc(SysUserPosition::getCreateTime));
        Map<Long, List<UserPositionBindingVO>> result = new HashMap<>();
        Map<Long, List<SysUserPosition>> grouped = bindings.stream()
                .collect(Collectors.groupingBy(SysUserPosition::getUserId));
        for (Map.Entry<Long, List<SysUserPosition>> entry : grouped.entrySet()) {
            result.put(entry.getKey(), toBindingVOs(entry.getValue()));
        }
        return result;
    }

    /**
     * 删除用户全部职位绑定。
     *
     * @param userId 用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeByUserId(Long userId) {
        sysUserPositionMapper.delete(new LambdaQueryWrapper<SysUserPosition>().eq(SysUserPosition::getUserId, userId));
    }

    /**
     * 判断职位是否仍有关联用户。
     *
     * @param positionId 职位 ID
     * @return 是否存在关联用户
     */
    public boolean hasUsersInPosition(Long positionId) {
        return sysUserPositionMapper.countUsersByPositionId(positionId) > 0;
    }

    private void validateBindingsForUser(Long userId, List<UserPositionBindingRequest> bindings) {
        if (bindings.isEmpty()) {
            return;
        }
        Set<Long> userDeptIds = new HashSet<>(departmentOperations.findDepartmentIdsByUser(userId));
        Set<String> uniquePairs = new HashSet<>();
        for (UserPositionBindingRequest binding : bindings) {
            if (!userDeptIds.contains(binding.departmentId())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "用户未绑定部门: " + binding.departmentId());
            }
            String pairKey = binding.departmentId() + ":" + binding.positionId();
            if (!uniquePairs.add(pairKey)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "职位绑定重复");
            }
            departmentValidator.requirePositionForDepartment(binding.departmentId(), binding.positionId());
        }
    }

    private List<UserPositionBindingRequest> normalizeBindings(List<UserPositionBindingRequest> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return List.of();
        }
        List<UserPositionBindingRequest> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (UserPositionBindingRequest binding : bindings) {
            if (binding == null || binding.departmentId() == null || binding.positionId() == null) {
                continue;
            }
            String key = binding.departmentId() + ":" + binding.positionId();
            if (seen.add(key)) {
                result.add(binding);
            }
        }
        return result;
    }

    private List<UserPositionBindingVO> toBindingVOs(List<SysUserPosition> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return List.of();
        }
        List<Long> deptIds = bindings.stream().map(SysUserPosition::getDepartmentId).filter(Objects::nonNull).distinct()
                .toList();
        List<Long> positionIds = bindings.stream().map(SysUserPosition::getPositionId).distinct().toList();
        Map<Long, OrgRefVO> deptRefs = departmentOperations.loadDepartmentRefs(deptIds);
        Map<Long, OrgRefVO> positionRefs = loadPositionRefs(positionIds);
        return bindings.stream()
                .map(binding -> new UserPositionBindingVO(
                        deptRefs.get(binding.getDepartmentId()), positionRefs.get(binding.getPositionId())))
                .filter(vo -> vo.department() != null && vo.position() != null)
                .toList();
    }

    private Map<Long, OrgRefVO> loadPositionRefs(List<Long> positionIds) {
        if (positionIds == null || positionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ids = positionIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysPositionService.listByIds(ids).stream()
                .collect(Collectors.toMap(
                        SysPosition::getId,
                        position -> new OrgRefVO(
                                position.getId(), position.getPositionCode(), position.getPositionName())));
    }
}
