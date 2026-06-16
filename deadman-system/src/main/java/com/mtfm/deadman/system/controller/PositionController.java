package com.mtfm.deadman.system.controller;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.system.dto.org.CreatePositionRequest;
import com.mtfm.deadman.system.dto.org.UpdatePositionRequest;
import com.mtfm.deadman.system.service.PositionAdminService;
import com.mtfm.deadman.system.vo.org.PositionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 职位管理接口。
 */
@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionAdminService positionAdminService;

    /**
     * 职位列表
     *
     * @param departmentId 可选；传入时返回该部门职位及全局职位
     * @return 职位列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.Org).POSITION_LIST_READ)")
    public Result<List<PositionVO>> list(@RequestParam(required = false) Long departmentId) {
        return Result.ok(positionAdminService.listPositions(departmentId));
    }

    /**
     * 职位详情
     *
     * @param positionId 职位 ID
     * @return 职位详情
     */
    @GetMapping("/{positionId}")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.Org).POSITION_LIST_READ)")
    public Result<PositionVO> detail(@PathVariable Long positionId) {
        return Result.ok(positionAdminService.getPosition(positionId));
    }

    /**
     * 创建职位
     *
     * @param request 创建职位请求
     * @return 新建职位详情
     */
    @PostMapping
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.Org).POSITION_CREATE)")
    public Result<PositionVO> create(@Valid @RequestBody CreatePositionRequest request) {
        return Result.ok(positionAdminService.createPosition(request));
    }

    /**
     * 更新职位
     *
     * @param positionId 职位 ID
     * @param request    更新职位请求（null 字段表示不修改）
     * @return 更新后的职位详情
     */
    @PutMapping("/{positionId}")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.Org).POSITION_UPDATE)")
    public Result<PositionVO> update(@PathVariable Long positionId, @Valid @RequestBody UpdatePositionRequest request) {
        return Result.ok(positionAdminService.updatePosition(positionId, request));
    }

    /**
     * 删除职位
     *
     * @param positionId 职位 ID
     * @return 空响应
     */
    @DeleteMapping("/{positionId}")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.Org).POSITION_DELETE)")
    public Result<Void> delete(@PathVariable Long positionId) {
        positionAdminService.deletePosition(positionId);
        return Result.ok();
    }
}
