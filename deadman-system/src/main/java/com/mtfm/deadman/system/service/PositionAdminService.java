package com.mtfm.deadman.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mtfm.deadman.common.enums.UserStatus;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.system.dto.org.CreatePositionRequest;
import com.mtfm.deadman.system.dto.org.UpdatePositionRequest;
import com.mtfm.deadman.system.entity.SysPosition;
import com.mtfm.deadman.system.vo.org.PositionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 职位管理。
 */
@Service
@RequiredArgsConstructor
public class PositionAdminService {

    private final SysPositionService sysPositionService;
    private final SysDepartmentService sysDepartmentService;
    private final UserPositionService userPositionService;

    /**
     * 查询职位列表。
     *
     * @param departmentId 可选；传入时返回该部门职位及全局职位
     * @return 职位列表
     */
    public List<PositionVO> listPositions(Long departmentId) {
        LambdaQueryWrapper<SysPosition> wrapper = new LambdaQueryWrapper<SysPosition>()
                .orderByAsc(SysPosition::getSortOrder)
                .orderByAsc(SysPosition::getPositionCode);
        if (departmentId != null) {
            wrapper.and(w -> w.eq(SysPosition::getDepartmentId, departmentId).or().isNull(SysPosition::getDepartmentId));
        }
        return sysPositionService.list(wrapper).stream().map(this::toVO).toList();
    }

    /**
     * 查询职位详情。
     *
     * @param positionId 职位 ID
     * @return 职位详情
     */
    public PositionVO getPosition(Long positionId) {
        return toVO(sysPositionService.requireById(positionId));
    }

    /**
     * 创建职位。
     *
     * @param request 创建请求
     * @return 新建职位详情
     */
    @Transactional(rollbackFor = Exception.class)
    public PositionVO createPosition(CreatePositionRequest request) {
        if (sysPositionService.count(new LambdaQueryWrapper<SysPosition>()
                        .eq(SysPosition::getPositionCode, request.positionCode()))
                > 0) {
            throw new BusinessException(ResultCode.POSITION_CODE_EXISTS);
        }
        if (request.departmentId() != null) {
            sysDepartmentService.requireById(request.departmentId());
        }

        SysPosition position = SysPosition.builder()
                .departmentId(request.departmentId())
                .positionCode(request.positionCode())
                .positionName(request.positionName())
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .status(UserStatus.ACTIVE.getValue())
                .build();
        sysPositionService.save(position);
        return toVO(position);
    }

    /**
     * 更新职位。
     *
     * @param positionId 职位 ID
     * @param request    更新请求
     * @return 更新后的职位详情
     */
    @Transactional(rollbackFor = Exception.class)
    public PositionVO updatePosition(Long positionId, UpdatePositionRequest request) {
        SysPosition position = sysPositionService.requireById(positionId);

        if (request.departmentId() != null) {
            sysDepartmentService.requireById(request.departmentId());
            position.setDepartmentId(request.departmentId());
        }
        if (request.positionName() != null) {
            position.setPositionName(request.positionName());
        }
        if (request.sortOrder() != null) {
            position.setSortOrder(request.sortOrder());
        }
        if (request.status() != null) {
            validateStatus(request.status());
            position.setStatus(request.status());
        }

        sysPositionService.updateById(position);
        return toVO(position);
    }

    /**
     * 删除职位（存在关联用户时不允许）。
     *
     * @param positionId 职位 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePosition(Long positionId) {
        sysPositionService.requireById(positionId);
        if (userPositionService.hasUsersInPosition(positionId)) {
            throw new BusinessException(ResultCode.POSITION_HAS_USERS);
        }
        sysPositionService.removeById(positionId);
    }

    private void validateStatus(Integer status) {
        if (status != UserStatus.ACTIVE.getValue() && status != UserStatus.DISABLED.getValue()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "状态仅支持 0-禁用 或 1-正常");
        }
    }

    private PositionVO toVO(SysPosition position) {
        return new PositionVO(
                position.getId(),
                position.getDepartmentId(),
                position.getPositionCode(),
                position.getPositionName(),
                position.getSortOrder(),
                position.getStatus(),
                position.getCreateTime(),
                position.getUpdateTime());
    }
}
