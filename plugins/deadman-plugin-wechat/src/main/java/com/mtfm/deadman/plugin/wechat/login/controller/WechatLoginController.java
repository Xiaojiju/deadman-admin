package com.mtfm.deadman.plugin.wechat.login.controller;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.plugin.wechat.login.WechatLoginApiConstants;
import com.mtfm.deadman.plugin.wechat.login.WechatLoginService;
import com.mtfm.deadman.plugin.wechat.login.dto.WechatLoginResolveRequest;
import com.mtfm.deadman.plugin.wechat.login.initiate.WechatLoginInitiateResult;
import com.mtfm.deadman.plugin.wechat.login.session.WechatLoginSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 微信登录统一公开 HTTP 接口。
 */
@RestController
@RequestMapping(WechatLoginApiConstants.API_BASE_PATH)
@RequiredArgsConstructor
public class WechatLoginController {

    private final WechatLoginService wechatLoginService;

    /**
     * 列出当前已注册的微信登录方式。
     *
     * @return 登录方式标识列表
     */
    @GetMapping("/kinds")
    public Result<List<String>> kinds() {
        return Result.ok(List.copyOf(wechatLoginService.supportedLoginKinds()));
    }

    /**
     * 发起指定登录方式的预登录流程（如网页扫码授权 URL）。
     *
     * @param loginKind 登录方式标识
     * @return 方式特有的发起结果模板对象
     */
    @GetMapping("/{loginKind}/initiate")
    public Result<WechatLoginInitiateResult> initiate(@PathVariable String loginKind) {
        return Result.ok(wechatLoginService.initiate(loginKind));
    }

    /**
     * 将登录凭证解析为统一会话模板对象。
     *
     * @param request 解析请求
     * @return 登录会话（子类型含方式特有字段）
     */
    @PostMapping("/sessions")
    public Result<WechatLoginSession> resolveSession(@Valid @RequestBody WechatLoginResolveRequest request) {
        return Result.ok(wechatLoginService.resolve(request));
    }
}
