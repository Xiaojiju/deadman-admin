package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.util.DedupUtils;
import com.mtfm.deadman.system.entity.SysUserPosition;
import com.mtfm.deadman.system.mapper.SysUserPositionMapper;
import com.mtfm.deadman.system.vo.org.OrgRefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户职位绑定服务。
 */
@Service
@RequiredArgsConstructor
public class UserPositionService {

    private final SysUserPositionMapper sysUserPositionMapper;
    private final UserOrgService userOrgService;

    /**
     * 查询用户已绑定的职位 ID 列表。
     *
     * @param userId 用户 ID
     * @return 职位 ID 列表
     */
    public List<Long> getPositionIdsByUserId(Long userId) {
        return sysUserPositionMapper.selectPositionIdsByUserId(userId);
    }

    /**
     * 查询用户已绑定的职位引用列表。
     *
     * @param userId 用户 ID
     * @return 职位引用列表
     */
    public List<OrgRefVO> getPositionRefsByUserId(Long userId) {
        return toPositionRefs(getPositionIdsByUserId(userId));
    }

    /**
     * 批量加载用户职位引用。
     *
     * @param userIds 用户 ID 列表
     * @return 用户 ID 到职位引用列表的映射
     */
    public Map<Long, List<OrgRefVO>> loadPositionRefsByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SysUserPosition> bindings = sysUserPositionMapper.selectList(
                new LambdaQueryWrapper<SysUserPosition>().in(SysUserPosition::getUserId, userIds));
        Map<Long, OrgRefVO> positionRefMap = userOrgService.loadPositionRefs(bindings.stream()
                .map(SysUserPosition::getPositionId)
                .distinct()
                .toList());
        return bindings.stream()
                .collect(Collectors.groupingBy(
                        SysUserPosition::getUserId,
                        Collectors.mapping(
                                b -> positionRefMap.get(b.getPositionId()),
                                Collectors.filtering(ref -> ref != null, Collectors.toList()))));
    }

    /**
     * 覆盖式绑定用户职位。
     *
     * @param userId       用户 ID
     * @param departmentId 用户所属部门 ID（用于校验职位归属）
     * @param positionIds  职位 ID 列表，空列表表示清空
     */
    @Transactional(rollbackFor = Exception.class)
    public void replaceUserPositions(Long userId, Long departmentId, List<Long> positionIds) {
        List<Long> normalized = DedupUtils.dedupeLongs(positionIds);
        userOrgService.validatePositionsForDepartment(departmentId, normalized);

        sysUserPositionMapper.delete(new LambdaQueryWrapper<SysUserPosition>().eq(SysUserPosition::getUserId, userId));
        for (Long positionId : normalized) {
            sysUserPositionMapper.insert(
                    SysUserPosition.builder().userId(userId).positionId(positionId).build());
        }
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

    private List<OrgRefVO> toPositionRefs(List<Long> positionIds) {
        if (positionIds == null || positionIds.isEmpty()) {
            return List.of();
        }
        Map<Long, OrgRefVO> refMap = userOrgService.loadPositionRefs(positionIds);
        return positionIds.stream().map(refMap::get).filter(ref -> ref != null).toList();
    }
}
