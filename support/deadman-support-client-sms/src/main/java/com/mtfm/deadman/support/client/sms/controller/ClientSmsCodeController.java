package com.mtfm.deadman.support.client.sms.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.plugin.sms.SmsCodeService;
import com.mtfm.deadman.plugin.sms.SmsScenes;
import com.mtfm.deadman.support.client.sms.dto.SendLoginSmsRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 用户端登录验证码。该路径由独立安全链匿名放行。
 */
@RestController
@RequestMapping("/client/api/auth/sms")
@RequiredArgsConstructor
public class ClientSmsCodeController {

    private final SmsCodeService smsCodeService;

    /**
     * 发送登录验证码。
     *
     * @param request 手机号
     * @return 空结果
     */
    @PostMapping("/send")
    public Result<Void> sendLoginCode(@Valid @RequestBody SendLoginSmsRequest request) {
        smsCodeService.send(SmsScenes.LOGIN, request.phone());
        return Result.ok();
    }
}
