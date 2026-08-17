package com.mtfm.deadman.support.client.wechat.controller;

import com.mtfm.deadman.common.auth.AllowAnonymous;
import com.mtfm.deadman.common.auth.AuthRealm;
import com.mtfm.deadman.common.auth.RequireAuth;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.component.client.service.ClientUserAccountService;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatBindPhoneRequest;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatFaceVerifyInitiateRequest;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatFaceVerifyQueryRequest;
import com.mtfm.deadman.plugin.wechat.miniprogram.spi.WechatFaceVerifyHandler;
import com.mtfm.deadman.plugin.wechat.miniprogram.spi.WechatPhoneBindingHandler;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatBindPhoneVO;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatBoundPhoneVO;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatFaceVerifyInitiateVO;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatFaceVerifyQueryVO;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatResolvedPhoneVO;
import com.mtfm.deadman.support.client.wechat.dto.ClientWechatBindOpenidRequest;
import com.mtfm.deadman.support.client.wechat.service.ClientWechatAuthService;
import com.mtfm.deadman.support.client.wechat.vo.ClientWechatBindingStatusVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端微信小程序扩展接口（手机号换号/绑定等），由 support 桥接模块提供。
 */
@RestController
@RequestMapping("/client/api/wechat-miniprogram")
@RequireAuth(AuthRealm.CLIENT)
@RequiredArgsConstructor
public class ClientWechatMiniprogramController {

    private final List<WechatPhoneBindingHandler> phoneBindingHandlers;
    private final List<WechatFaceVerifyHandler> faceVerifyHandlers;
    private final ClientUserAccountService clientUserAccountService;
    private final ClientWechatAuthService clientWechatAuthService;

    /**
     * 使用 getPhoneNumber code 向微信换取手机号（不落库，供注册页回填）。
     * <p>
     * 匿名可访问；code 由小程序授权按钮产生，短时效且一次性。
     *
     * @param request 含手机号动态令牌
     * @return 明文手机号
     */
    @AllowAnonymous
    @PostMapping("/phone/resolve")
    public Result<WechatResolvedPhoneVO> resolvePhone(@Valid @RequestBody WechatBindPhoneRequest request) {
        return Result.ok(requirePhoneHandler().resolvePhone(request));
    }

    /**
     * 查询当前用户是否已绑定微信小程序（OAuth openid）。
     *
     * @param loginUser 当前登录用户
     * @return 绑定状态；bound=true 表示已绑定
     */
    @GetMapping("/binding")
    public Result<ClientWechatBindingStatusVO> getBindingStatus(
            @AuthenticationPrincipal ClientLoginUser loginUser) {
        boolean bound = clientUserAccountService.findMiniprogramOpenid(loginUser.getUserId()).isPresent();
        return Result.ok(new ClientWechatBindingStatusVO(bound));
    }

    /**
     * 已登录用户使用 wx.login code 绑定当前微信小程序 openid。
     *
     * @param loginUser 当前登录用户
     * @param request   含 wx.login code
     * @return 绑定状态；成功后 bound=true
     */
    @PostMapping("/binding")
    public Result<ClientWechatBindingStatusVO> bindMiniprogram(
            @AuthenticationPrincipal ClientLoginUser loginUser,
            @Valid @RequestBody ClientWechatBindOpenidRequest request) {
        return Result.ok(clientWechatAuthService.bindMiniprogramForCurrentUser(
                loginUser.getUserId(), request.code()));
    }

    /**
     * 查询当前用户已绑定的手机号（脱敏）。
     *
     * @param loginUser 当前登录用户
     * @return 已绑定手机号；未绑定时 phone 为 null
     */
    @GetMapping("/phone")
    public Result<WechatBoundPhoneVO> getBoundPhone(@AuthenticationPrincipal ClientLoginUser loginUser) {
        return Result.ok(requirePhoneHandler().getBoundPhone(loginUser.getUserId()));
    }

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
        return Result.ok(requirePhoneHandler().bindPhone(loginUser.getUserId(), request));
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
        WechatFaceVerifyHandler handler = faceVerifyHandlers.stream()
                .filter(item -> ClientAuthConstants.LOGIN_GROUP_ID.equals(item.loginGroupId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户端微信人脸核身未配置"));
        return Result.ok(handler.initiateVerify(loginUser.getUserId(), request));
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
        WechatFaceVerifyHandler handler = faceVerifyHandlers.stream()
                .filter(item -> ClientAuthConstants.LOGIN_GROUP_ID.equals(item.loginGroupId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户端微信人脸核身未配置"));
        return Result.ok(handler.queryVerifyResult(loginUser.getUserId(), request));
    }

    private WechatPhoneBindingHandler requirePhoneHandler() {
        return phoneBindingHandlers.stream()
                .filter(item -> ClientAuthConstants.LOGIN_GROUP_ID.equals(item.loginGroupId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户端微信手机号绑定未配置"));
    }
}
