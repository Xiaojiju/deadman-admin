package com.mtfm.deadman.component.client.controller;

import com.mtfm.deadman.common.page.PageVO;
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.component.client.dto.ClientUserAdminPageQuery;
import com.mtfm.deadman.component.client.service.ClientUserAdminService;
import com.mtfm.deadman.component.client.vo.ClientUserAdminDetailVO;
import com.mtfm.deadman.component.client.vo.ClientUserAdminSummaryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端对用户端用户的查询、禁用与注销（走管理端 JWT 与权限体系）。
 */
@RestController
@RequestMapping("/api/client-users")
@RequiredArgsConstructor
public class ClientUserAdminController {

    private final ClientUserAdminService clientUserAdminService;

    /**
     * 用户端用户分页列表。
     *
     * @param query 分页与筛选
     * @return 分页数据
     */
    @GetMapping
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.component.client.permission.ClientPermissions).LIST_READ)")
    public Result<PageVO<ClientUserAdminSummaryVO>> page(@Valid ClientUserAdminPageQuery query) {
        return Result.ok(clientUserAdminService.pageUsers(query));
    }

    /**
     * 用户端用户详情。
     *
     * @param userId 用户 ID
     * @return 用户详情
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.component.client.permission.ClientPermissions).LIST_READ)")
    public Result<ClientUserAdminDetailVO> detail(@PathVariable Long userId) {
        return Result.ok(clientUserAdminService.getUserDetail(userId));
    }

    /**
     * 禁用用户端用户。
     *
     * @param userId 用户 ID
     * @return 更新后详情
     */
    @PutMapping("/{userId}/disable")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.component.client.permission.ClientPermissions).UPDATE)")
    public Result<ClientUserAdminDetailVO> disable(@PathVariable Long userId) {
        return Result.ok(clientUserAdminService.disableUser(userId));
    }

    /**
     * 注销（逻辑删除）用户端用户。
     *
     * @param userId 用户 ID
     * @return 空结果
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.component.client.permission.ClientPermissions).DELETE)")
    public Result<Void> delete(@PathVariable Long userId) {
        clientUserAdminService.deleteUser(userId);
        return Result.ok();
    }
}
