package com.mtfm.deadman.support.client.wechat.controller;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.client.ClientAuthSupport;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatBindPhoneRequest;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatFaceVerifyInitiateRequest;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatFaceVerifyQueryRequest;
import com.mtfm.deadman.plugin.wechat.miniprogram.spi.WechatFaceVerifyHandler;
import com.mtfm.deadman.plugin.wechat.miniprogram.spi.WechatPhoneBindingHandler;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatBindPhoneVO;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatFaceVerifyInitiateVO;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatFaceVerifyQueryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端微信小程序扩展接口（手机号绑定等），由 support 桥接模块提供。
 */
@RestController
@RequestMapping("/client/api/wechat-miniprogram")
@RequiredArgsConstructor
public class ClientWechatMiniprogramController {

    private final List<WechatPhoneBindingHandler> phoneBindingHandlers;
    private final List<WechatFaceVerifyHandler> faceVerifyHandlers;

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

    /**
     * 发起人脸核身，获取 verifyId 供小程序调用 wx.requestFacialVerify。
     *
     * @param loginUser 当前登录用户
     * @param request   实名信息与可选 code
     * @return verifyId、有效期与业务流水号
     */
    @PostMapping("/face-verify/initiate")
    public Result<WechatFaceVerifyInitiateVO> initiateFaceVerify(
            @AuthenticationPrincipal ClientLoginUser loginUser,
            @Valid @RequestBody WechatFaceVerifyInitiateRequest request) {
        ClientLoginUser user = ClientAuthSupport.requireLogin(loginUser);
        WechatFaceVerifyHandler handler = faceVerifyHandlers.stream()
                .filter(item -> ClientAuthConstants.LOGIN_GROUP_ID.equals(item.loginGroupId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户端微信人脸核身未配置"));
        return Result.ok(handler.initiateVerify(user.getUserId(), request));
    }

    /**
     * 查询人脸核身结果。
     *
     * @param loginUser 当前登录用户
     * @param request   查询参数
     * @return 核身结果
     */
    @PostMapping("/face-verify/query")
    public Result<WechatFaceVerifyQueryVO> queryFaceVerify(
            @AuthenticationPrincipal ClientLoginUser loginUser,
            @Valid @RequestBody WechatFaceVerifyQueryRequest request) {
        ClientLoginUser user = ClientAuthSupport.requireLogin(loginUser);
        WechatFaceVerifyHandler handler = faceVerifyHandlers.stream()
                .filter(item -> ClientAuthConstants.LOGIN_GROUP_ID.equals(item.loginGroupId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户端微信人脸核身未配置"));
        return Result.ok(handler.queryVerifyResult(user.getUserId(), request));
    }
}
