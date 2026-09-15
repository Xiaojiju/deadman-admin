package com.mtfm.deadman.security.controller;

import com.mtfm.deadman.common.auth.AuthRealm;
import com.mtfm.deadman.common.auth.RequireAuth;
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.security.LoginUser;
import com.mtfm.deadman.system.dto.user.UpdateUserRequest;
import com.mtfm.deadman.system.service.UserService;
import com.mtfm.deadman.system.vo.user.UserProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前登录用户资料接口。
 */
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户资料（含主用户名、用户状态、已绑定账号及第三方 OAuth 类型）。
     *
     * @param loginUser 登录用户
     * @return 用户资料
     */
    @GetMapping
    @PreAuthorize("hasAuthority('user:profile:read')")
    @RequireAuth(AuthRealm.ADMIN)
    public Result<UserProfileVO> me(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.ok(userService.getProfileByUserCode(loginUser.getUserCode()));
    }

    /**
     * 更新当前用户本人资料（请求体同管理端 {@link UpdateUserRequest}，仅生效 nickname、avatar、phone）。
     *
     * @param loginUser 登录用户
     * @param request   更新请求
     * @return 更新后的用户资料
     */
    @PutMapping
    @PreAuthorize("hasAuthority('user:profile:update')")
    @RequireAuth(AuthRealm.ADMIN)
    public Result<UserProfileVO> updateMe(
            @AuthenticationPrincipal LoginUser loginUser, @Valid @RequestBody UpdateUserRequest request) {
        return Result.ok(userService.updateProfileByUserCode(loginUser.getUserCode(), request));
    }
}
