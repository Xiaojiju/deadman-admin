package com.mtfm.deadman.component.client.controller;

import com.mtfm.deadman.common.auth.AuthRealm;
import com.mtfm.deadman.common.auth.RequireAuth;
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.component.client.service.ClientUserService;
import com.mtfm.deadman.component.client.vo.ClientUserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端当前用户接口。
 */
@RestController
@RequestMapping("/client/api/users")
@RequiredArgsConstructor
public class ClientUserController {

    private final ClientUserService clientUserService;

    /**
     * 获取当前用户资料。
     *
     * @param loginUser 当前登录用户
     * @return 用户资料
     */
    @GetMapping("/me")
    @RequireAuth(AuthRealm.CLIENT)
    public Result<ClientUserProfileVO> currentProfile(@AuthenticationPrincipal ClientLoginUser loginUser) {
        return Result.ok(clientUserService.getProfileByUserCode(loginUser.getUserCode()));
    }
}
