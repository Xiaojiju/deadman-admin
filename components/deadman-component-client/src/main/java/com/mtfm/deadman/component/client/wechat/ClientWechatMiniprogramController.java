package com.mtfm.deadman.component.client.wechat;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.client.ClientAuthSupport;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatBindPhoneRequest;
import com.mtfm.deadman.plugin.wechat.miniprogram.spi.WechatPhoneBindingHandler;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatBindPhoneVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端微信小程序扩展接口（手机号绑定等），仅在同时引入 client 与 wechat 插件时装配。
 */
@RestController
@RequestMapping("/client/api/wechat-miniprogram")
@RequiredArgsConstructor
public class ClientWechatMiniprogramController {

    private final List<WechatPhoneBindingHandler> phoneBindingHandlers;

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
        ClientLoginUser user = ClientAuthSupport.requireLogin(loginUser);
        WechatPhoneBindingHandler handler = phoneBindingHandlers.stream()
                .filter(item -> ClientAuthConstants.LOGIN_GROUP_ID.equals(item.loginGroupId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户端微信手机号绑定未配置"));
        return Result.ok(handler.bindPhone(user.getUserId(), request));
    }
}
