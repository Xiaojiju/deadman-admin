package com.mtfm.deadman.component.openauth.controller;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.component.openauth.dto.CreateOpenAppRequest;
import com.mtfm.deadman.component.openauth.dto.UpdateOpenAppRequest;
import com.mtfm.deadman.component.openauth.service.OpenAppAdminService;
import com.mtfm.deadman.component.openauth.vo.CreateOpenAppResultVO;
import com.mtfm.deadman.component.openauth.vo.OpenAppSummaryVO;
import com.mtfm.deadman.component.openauth.vo.RotateOpenAppSecretResultVO;
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

import java.util.List;

/**
 * 管理端开放应用管理接口。
 */
@RestController
@RequestMapping("/api/open-apps")
@RequiredArgsConstructor
public class OpenAppAdminController {

    private final OpenAppAdminService openAppAdminService;

    /**
     * 查询开放应用列表。
     *
     * @return 应用列表
     */
    @GetMapping
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.component.openauth.permission.OpenAuthPermissions).LIST_READ)")
    public Result<List<OpenAppSummaryVO>> list() {
        return Result.ok(openAppAdminService.listApps());
    }

    /**
     * 创建开放应用。
     *
     * @param request 创建请求
     * @return 创建结果（含一次性 client_secret）
     */
    @PostMapping
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.component.openauth.permission.OpenAuthPermissions).CREATE)")
    public Result<CreateOpenAppResultVO> create(@Valid @RequestBody CreateOpenAppRequest request) {
        return Result.ok(openAppAdminService.createApp(request));
    }

    /**
     * 更新开放应用。
     *
     * @param id      主键
     * @param request 更新请求
     * @return 更新后摘要
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.component.openauth.permission.OpenAuthPermissions).UPDATE)")
    public Result<OpenAppSummaryVO> update(@PathVariable Long id, @Valid @RequestBody UpdateOpenAppRequest request) {
        return Result.ok(openAppAdminService.updateApp(id, request));
    }

    /**
     * 轮换开放应用密钥。
     *
     * @param id 主键
     * @return 新密钥（仅展示一次）
     */
    @PostMapping("/{id}/rotate-secret")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.component.openauth.permission.OpenAuthPermissions).SECRET_ROTATE)")
    public Result<RotateOpenAppSecretResultVO> rotateSecret(@PathVariable Long id) {
        return Result.ok(openAppAdminService.rotateSecret(id));
    }

    /**
     * 删除开放应用。
     *
     * @param id 主键
     * @return 空结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.component.openauth.permission.OpenAuthPermissions).DELETE)")
    public Result<Void> delete(@PathVariable Long id) {
        openAppAdminService.deleteApp(id);
        return Result.ok();
    }
}
