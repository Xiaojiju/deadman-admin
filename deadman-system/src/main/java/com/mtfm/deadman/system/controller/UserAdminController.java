package com.mtfm.deadman.system.controller;

import com.mtfm.deadman.common.page.PageVO;
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.system.domain.user.UserOperations;
import com.mtfm.deadman.system.dto.role.AssignUserRolesRequest;
import com.mtfm.deadman.system.dto.user.CreateUserRequest;
import com.mtfm.deadman.system.dto.user.ResetUserPasswordRequest;
import com.mtfm.deadman.system.dto.user.UpdateUserRequest;
import com.mtfm.deadman.system.dto.user.UserAdminPageQuery;
import com.mtfm.deadman.system.service.UserAdminReadService;
import com.mtfm.deadman.system.service.UserAdminViewAssembler;
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

    private final UserAdminReadService userAdminReadService;
    private final UserAdminViewAssembler viewAssembler;
    private final UserOperations userOperations;

    /**
     * 用户分页列表
     *
     * @param query 用户分页查询参数
     * @return 用户分页列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.User).LIST_READ)")
    public Result<PageVO<UserAdminSummaryVO>> page(@Valid UserAdminPageQuery query) {
        var page = userAdminReadService.pageUserRecords(query);
        if (page.getRecords().isEmpty()) {
            return Result.ok(PageVO.of(java.util.List.of(), page.getTotal(), query));
        }
        return Result.ok(PageVO.of(viewAssembler.assembleSummaries(page.getRecords()), page.getTotal(), query));
    }

    /**
     * 用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.User).LIST_READ)")
    public Result<UserAdminDetailVO> detail(@PathVariable Long userId) {
        return Result.ok(userAdminReadService.getUserDetail(userId));
    }

    /**
     * 新增用户
     *
     * @param request 新增用户请求
     * @return 新增用户
     */
    @PostMapping
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.User).CREATE)")
    public Result<UserAdminDetailVO> create(@Valid @RequestBody CreateUserRequest request) {
        Long userId = userOperations.createUser(request);
        return Result.ok(userAdminReadService.getUserDetail(userId));
    }

    /**
     * 更新用户
     *
     * @param userId  用户ID
     * @param request 更新用户请求
     * @return 更新用户
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.User).UPDATE)")
    public Result<UserAdminDetailVO> update(@PathVariable Long userId, @Valid @RequestBody UpdateUserRequest request) {
        userOperations.updateUser(userId, request);
        return Result.ok(userAdminReadService.getUserDetail(userId));
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 删除用户
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.User).DELETE)")
    public Result<Void> delete(@PathVariable Long userId) {
        userOperations.deleteUser(userId);
        return Result.ok();
    }

    /**
     * 重置用户密码（无需原密码，用于用户忘记密码等场景）。
     *
     * @param userId  用户 ID
     * @param request 新密码
     */
    @PutMapping("/{userId}/password")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.User).PASSWORD_RESET)")
    public Result<Void> resetPassword(
            @PathVariable Long userId, @Valid @RequestBody ResetUserPasswordRequest request) {
        userOperations.resetUserPassword(userId, request.newPassword());
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
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.system.permission.SystemPermissions.Role).USER_ASSIGN)")
    public Result<UserAdminDetailVO> assignRoles(
            @PathVariable Long userId, @Valid @RequestBody AssignUserRolesRequest request) {
        userOperations.assignUserRoles(userId, request.roleIds());
        return Result.ok(userAdminReadService.getUserDetail(userId));
    }
}
