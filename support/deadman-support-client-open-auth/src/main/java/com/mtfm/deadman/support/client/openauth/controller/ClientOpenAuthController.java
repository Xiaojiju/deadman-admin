package com.mtfm.deadman.support.client.openauth.controller;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.component.client.ClientAuthSupport;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.component.openauth.dto.OpenAuthCodeRequest;
import com.mtfm.deadman.component.openauth.service.OpenAuthFacadeService;
import com.mtfm.deadman.component.openauth.vo.OpenAuthCodeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端开放授权接口，供已登录用户申请 auth_code。
 */
@RestController
@RequestMapping("/client/api/open")
@RequiredArgsConstructor
public class ClientOpenAuthController {

    private final OpenAuthFacadeService openAuthFacadeService;

    /**
     * 申请开放授权码，供第三方应用兑换 open_access_token。
     *
     * @param loginUser 当前登录用户
     * @param request   目标应用 AppId
     * @return 授权码
     */
    @PostMapping("/codes")
    public Result<OpenAuthCodeVO> issueCode(
            @AuthenticationPrincipal ClientLoginUser loginUser, @Valid @RequestBody OpenAuthCodeRequest request) {
        ClientLoginUser user = ClientAuthSupport.requireLogin(loginUser);
        return Result.ok(openAuthFacadeService.issueAuthCode(
                ClientAuthConstants.LOGIN_GROUP_ID,
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()),
                request.appId()));
    }
}
