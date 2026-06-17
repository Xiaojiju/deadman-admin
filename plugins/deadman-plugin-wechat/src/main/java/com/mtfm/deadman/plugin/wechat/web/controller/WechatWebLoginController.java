package com.mtfm.deadman.plugin.wechat.web.controller;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.plugin.wechat.login.WechatLoginKinds;
import com.mtfm.deadman.plugin.wechat.login.WechatLoginService;
import com.mtfm.deadman.plugin.wechat.login.initiate.WechatWebLoginInitiateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信网页扫码登录辅助接口（兼容旧路径，委托统一 {@link WechatLoginService}）。
 *
 * @deprecated 请使用 {@code GET /api/wechat/login/wechat-web/initiate}
 */
@Deprecated
@RestController
@RequestMapping("/api/wechat-web")
@RequiredArgsConstructor
public class WechatWebLoginController {

    private final WechatLoginService wechatLoginService;

    /**
     * 获取微信开放平台扫码登录授权页地址。
     *
     * @return 授权地址与 state
     * @deprecated 请使用 {@code GET /api/wechat/login/wechat-web/initiate}
     */
    @Deprecated
    @GetMapping("/authorize-url")
    public Result<WechatWebLoginInitiateResult> authorizeUrl() {
        return Result.ok((WechatWebLoginInitiateResult) wechatLoginService.initiate(WechatLoginKinds.WEB));
    }
}
