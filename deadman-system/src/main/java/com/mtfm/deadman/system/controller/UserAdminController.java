package com.mtfm.deadman.system.controller;

import com.mtfm.deadman.common.page.PageVO;
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.system.dto.role.AssignUserRolesRequest;
import com.mtfm.deadman.system.dto.user.CreateUserRequest;
import com.mtfm.deadman.system.dto.user.ResetUserPasswordRequest;
import com.mtfm.deadman.system.dto.user.UpdateUserRequest;
import com.mtfm.deadman.system.dto.user.UserAdminPageQuery;
import com.mtfm.deadman.system.service.UserAdminService;
import com.mtfm.deadman.system.vo.user.UserAdminDetailVO;
import com.mtfm.deadman.system.vo.user.UserAdminSummaryVO;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端用户 CRUD、停用与角色关联。
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    /**
     * 用户分页列表
     *
     * @param query 用户分页查询参数
     * @return 用户分页列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority('user:list:read')")
    public Result<PageVO<UserAdminSummaryVO>> page(@Valid UserAdminPageQuery query) {
        return Result.ok(userAdminService.pageUsers(query));
    }

    /**
     * 用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:list:read')")
    public Result<UserAdminDetailVO> detail(@PathVariable Long userId) {
        return Result.ok(userAdminService.getUserDetail(userId));
    }

    /**
     * 新增用户
     *
     * @param request 新增用户请求
     * @return 新增用户
     */
    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public Result<UserAdminDetailVO> create(@Valid @RequestBody CreateUserRequest request) {
        return Result.ok(userAdminService.createUser(request));
    }

    /**
     * 更新用户
     *
     * @param userId  用户ID
     * @param request 更新用户请求
     * @return 更新用户
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:update')")
    public Result<UserAdminDetailVO> update(@PathVariable Long userId, @Valid @RequestBody UpdateUserRequest request) {
        return Result.ok(userAdminService.updateUser(userId, request));
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 删除用户
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:delete')")
    public Result<Void> delete(@PathVariable Long userId) {
        userAdminService.deleteUser(userId);
        return Result.ok();
    }

    /**
     * 重置用户密码（无需原密码，用于用户忘记密码等场景）。
     *
     * @param userId  用户 ID
     * @param request 新密码
     */
    @PutMapping("/{userId}/password")
    @PreAuthorize("hasAuthority('user:password:reset')")
    public Result<Void> resetPassword(
            @PathVariable Long userId, @Valid @RequestBody ResetUserPasswordRequest request) {
        userAdminService.resetUserPassword(userId, request);
        return Result.ok();
    }

    /**
     * 分配用户角色
     *
     * @param userId  用户ID
     * @param request 分配用户角色请求
     * @return 分配用户角色
     */
    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('role:user:assign')")
    public Result<UserAdminDetailVO> assignRoles(
            @PathVariable Long userId, @Valid @RequestBody AssignUserRolesRequest request) {
        return Result.ok(userAdminService.assignUserRoles(userId, request));
    }
}
