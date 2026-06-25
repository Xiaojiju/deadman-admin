package com.mtfm.deadman.support.client.im.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.plugin.im.tencent.service.ImService;
import com.mtfm.deadman.plugin.im.tencent.vo.ImCredentialVO;
import com.mtfm.deadman.support.client.im.constant.ClientImRealmConstants;

import lombok.RequiredArgsConstructor;

/**
 * 用户端 IM 凭证 API，桥接腾讯云 IM 插件与 client 鉴权。
 */
@RestController
@RequestMapping("/client/api/im")
@RequiredArgsConstructor
public class ClientImController {

    private final ImService imService;

    /**
     * 为当前登录用户签发腾讯云 IM 登录凭证。
     *
     * @return IM 登录凭证
     */
    @GetMapping("/credential")
    public Result<ImCredentialVO> credential() {
        return Result.ok(imService.issueCredentialForCurrentUser(ClientImRealmConstants.REALM_ID));
    }
}
