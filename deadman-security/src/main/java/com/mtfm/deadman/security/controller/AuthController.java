package com.mtfm.deadman.security.controller;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.security.LoginUser;
import com.mtfm.deadman.security.SecurityAuthSupport;
import com.mtfm.deadman.security.dto.auth.ChangePasswordRequest;
import com.mtfm.deadman.security.dto.auth.RegisterRequest;
import com.mtfm.deadman.security.service.AuthCredentialsService;
import com.mtfm.deadman.security.service.AuthPermissionService;
import com.mtfm.deadman.security.vo.auth.AuthTokenVO;
import com.mtfm.deadman.security.vo.auth.UserAuthorityVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：注册、修改密码、获取当前用户权限。
 * <p>登录由 {@link com.mtfm.deadman.security.authentication.JsonUsernamePasswordAuthenticationFilter} 处理。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCredentialsService authCredentialsService;
    private final AuthPermissionService authPermissionService;

    /**
     * 注册
     * 
     * @param request 注册请求
     * @return 授权令牌
     */
    @PostMapping("/register")
    public Result<AuthTokenVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authCredentialsService.register(request));
    }

    /**
     * 获取当前用户权限
     * 
     * @param loginUser 登录用户
     * @return 用户权限
     */
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('auth:permissions:read')")
    public Result<UserAuthorityVO> currentPermissions(@AuthenticationPrincipal LoginUser loginUser) {
        LoginUser user = SecurityAuthSupport.requireLogin(loginUser);
        return Result.ok(authPermissionService.getUserAuthority(user.getUserId()));
    }

    /**
     * 修改密码
     * 
     * @param loginUser 登录用户
     * @param request   修改密码请求
     * @return 修改密码结果
     */
    @PutMapping("/password")
    @PreAuthorize("hasAuthority('auth:password:change')")
    public Result<Void> changePassword(
            @AuthenticationPrincipal LoginUser loginUser, @Valid @RequestBody ChangePasswordRequest request) {
        LoginUser user = SecurityAuthSupport.requireLogin(loginUser);
        authCredentialsService.changePassword(user.getUserId(), user.getUserCode(), request);
        return Result.ok();
    }
}
