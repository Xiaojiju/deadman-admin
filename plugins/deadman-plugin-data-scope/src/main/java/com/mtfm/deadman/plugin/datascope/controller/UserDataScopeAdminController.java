package com.mtfm.deadman.plugin.datascope.controller;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.common.spi.DataScopeUserBridge;
import com.mtfm.deadman.plugin.datascope.dto.AssignUserDataScopeRequest;
import com.mtfm.deadman.plugin.datascope.model.DataScopeProfile;
import com.mtfm.deadman.plugin.datascope.service.UserDataScopeProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户数据权限独立管理接口（与角色解耦）。
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserDataScopeAdminController {

    private final UserDataScopeProfileService profileService;
    private final DataScopeUserBridge userBridge;

    /**
     * 查询用户数据范围配置。
     *
     * @param userId 用户 ID
     * @return 数据权限配置
     */
    @GetMapping("/{userId}/data-scope")
    @PreAuthorize("hasAuthority('user:list:read')")
    public Result<DataScopeVO> getDataScope(@PathVariable Long userId) {
        userBridge.requireExists(userId);
        return Result.ok(toVO(userId, profileService.resolveProfile(userId)));
    }

    /**
     * 分配用户数据范围（覆盖式），并刷新运行时缓存。
     *
     * @param userId  用户 ID
     * @param request 数据范围请求
     * @return 更新后的数据权限配置
     */
    @PutMapping("/{userId}/data-scope")
    @PreAuthorize("hasAuthority('user:update')")
    public Result<DataScopeVO> assignDataScope(
            @PathVariable Long userId, @Valid @RequestBody AssignUserDataScopeRequest request) {
        userBridge.requireExists(userId);
        profileService.assignScope(userId, request.scopeType(), request.customDeptIds());
        return Result.ok(toVO(userId, profileService.resolveProfile(userId)));
    }

    private DataScopeVO toVO(Long userId, DataScopeProfile profile) {
        return new DataScopeVO(
                userId,
                profile.scopeType().name(),
                profile.customDeptIds().stream().sorted().toList());
    }

    /**
     * 用户数据权限配置视图。
     *
     * @param userId        用户 ID
     * @param scopeType     数据范围类型
     * @param customDeptIds CUSTOM 可见部门 ID 列表
     */
    public record DataScopeVO(Long userId, String scopeType, List<Long> customDeptIds) {
    }
}
