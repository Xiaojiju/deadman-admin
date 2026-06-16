package com.mtfm.deadman.component.client.controller;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.component.client.dto.ClientRegisterRequest;
import com.mtfm.deadman.component.client.service.ClientAuthCredentialsService;
import com.mtfm.deadman.security.vo.auth.RegisterResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端认证接口：注册。
 * <p>登录由各 {@link com.mtfm.deadman.component.client.spi.ClientLoginProvider} 的独立 Filter 处理。
 */
@RestController
@RequestMapping("/client/api/auth")
@RequiredArgsConstructor
public class ClientAuthController {

    private final ClientAuthCredentialsService clientAuthCredentialsService;

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
}
