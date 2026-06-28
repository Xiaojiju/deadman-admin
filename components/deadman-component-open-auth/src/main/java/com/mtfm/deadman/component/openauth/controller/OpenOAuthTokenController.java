package com.mtfm.deadman.component.openauth.controller;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.component.openauth.dto.OpenOAuthTokenRequest;
import com.mtfm.deadman.component.openauth.service.OpenAuthFacadeService;
import com.mtfm.deadman.component.openauth.vo.OpenOAuthTokenVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 开放 OAuth Token 端点，供第三方应用使用 client 凭证兑换 access_token。
 */
@RestController
@RequestMapping("/open-api/oauth")
@RequiredArgsConstructor
public class OpenOAuthTokenController {

    private final OpenAuthFacadeService openAuthFacadeService;

    /**
     * 使用 authorization_code 模式兑换 open_access_token。
     *
     * @param request 兑换请求
     * @return access_token 及主体信息
     */
    @PostMapping("/token")
    public Result<OpenOAuthTokenVO> token(@Valid @RequestBody OpenOAuthTokenRequest request) {
        return Result.ok(openAuthFacadeService.exchangeToken(request));
    }
}
