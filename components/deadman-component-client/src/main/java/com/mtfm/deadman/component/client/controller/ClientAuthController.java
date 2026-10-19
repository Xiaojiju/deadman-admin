package com.mtfm.deadman.component.client.controller;

import com.mtfm.deadman.common.auth.AuthRealm;
import com.mtfm.deadman.common.auth.RequireAuth;
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.component.client.auth.jwt.ClientAccessTokenRevocationStore;
import com.mtfm.deadman.component.client.auth.jwt.ClientJwtAuthenticationFilter;
import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.component.client.dto.ClientChangePasswordRequest;
import com.mtfm.deadman.component.client.dto.ClientRegisterRequest;
import com.mtfm.deadman.component.client.service.ClientAuthCredentialsService;
import com.mtfm.deadman.security.token.AuthTokenIssueProviderRegistry;
import com.mtfm.deadman.security.vo.auth.RegisterResultVO;
import io.jsonwebtoken.Claims;
import java.time.Duration;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端认证接口：注册、修改密码、登出。
 * <p>
 * 登录由各 {@link com.mtfm.deadman.component.client.spi.ClientLoginProvider} 的独立 Filter 处理。
 */
@RestController
@RequestMapping("/client/api/auth")
@RequiredArgsConstructor
public class ClientAuthController {

    private final ClientAuthCredentialsService clientAuthCredentialsService;
    private final AuthTokenIssueProviderRegistry providerRegistry;
    private final ClientAccessTokenRevocationStore revocationStore;

    /**
     * 用户端注册。
     *
     * @param request 注册请求
     * @return 注册结果（不含令牌）
     */
    @PostMapping("/register")
    public Result<RegisterResultVO> register(@Valid @RequestBody ClientRegisterRequest request) {
        return Result.ok(clientAuthCredentialsService.register(request));
    }

    /**
     * 修改当前用户密码。
     *
     * @param loginUser 当前登录用户
     * @param request 原密码与新密码
     * @return 空结果
     */
    @PutMapping("/password")
    @RequireAuth(AuthRealm.CLIENT)
    public Result<Void> changePassword(@AuthenticationPrincipal ClientLoginUser loginUser,
        @Valid @RequestBody ClientChangePasswordRequest request) {
        clientAuthCredentialsService.changePassword(loginUser.getUserId(), request);
        return Result.ok();
    }

    /**
     * 登出：吊销当前用户的 Refresh Token（每个用户只保留一个），并把当前这枚 Access Token 记为已登出直到它过期。
     * 之后旧 Refresh Token 调 {@code /client/api/auth/refresh} 返回 401 + 10008，旧 Access Token 调业务接口返回 401 + 40100。
     *
     * @param loginUser 当前登录用户
     * @return 空结果
     */
    @PostMapping("/logout")
    @RequireAuth(AuthRealm.CLIENT)
    public Result<Void> logout(@AuthenticationPrincipal ClientLoginUser loginUser,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        providerRegistry.require(ClientAuthConstants.JWT_REALM).invalidateUserSessions(loginUser.getUserId());
        Claims claims = ClientJwtAuthenticationFilter.parseAccessClaims(providerRegistry, authorization);
        if (claims != null && claims.getExpiration() != null) {
            revocationStore.revoke(claims.getId(),
                Duration.ofMillis(claims.getExpiration().getTime() - System.currentTimeMillis()));
        }
        return Result.ok();
    }
}
