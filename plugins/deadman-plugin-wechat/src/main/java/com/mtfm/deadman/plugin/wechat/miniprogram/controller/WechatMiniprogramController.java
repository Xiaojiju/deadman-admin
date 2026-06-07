package com.mtfm.deadman.plugin.wechat.miniprogram.controller;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatBindPhoneRequest;
import com.mtfm.deadman.plugin.wechat.miniprogram.service.WechatMiniprogramPhoneService;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatBindPhoneVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信小程序扩展接口（手机号绑定等）。
 */
@RestController
@RequestMapping("/client/api/wechat-miniprogram")
@ConditionalOnProperty(prefix = "deadman.plugin.wechat-miniprogram", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class WechatMiniprogramController {

    private final WechatMiniprogramPhoneService wechatMiniprogramPhoneService;

    /**
     * 绑定微信手机号到当前用户端账号。
     *
     * @param loginUser 当前登录用户
     * @param request   手机号 code
     * @return 绑定后的手机号
     */
    @PostMapping("/phone/bind")
    public Result<WechatBindPhoneVO> bindPhone(
            @AuthenticationPrincipal ClientLoginUser loginUser, @Valid @RequestBody WechatBindPhoneRequest request) {
        return Result.ok(wechatMiniprogramPhoneService.bindPhone(loginUser, request));
    }
}
