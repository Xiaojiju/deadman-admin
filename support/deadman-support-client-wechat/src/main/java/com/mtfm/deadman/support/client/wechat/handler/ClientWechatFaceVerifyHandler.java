package com.mtfm.deadman.support.client.wechat.handler;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.component.client.service.ClientUserAccountService;
import com.mtfm.deadman.plugin.wechat.miniprogram.WechatFaceVerifyConstants;
import com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatApiClient;
import com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatGetVerifyIdResult;
import com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatQueryVerifyInfoResult;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatFaceCertInfo;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatFaceVerifyInitiateRequest;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatFaceVerifyQueryRequest;
import com.mtfm.deadman.plugin.wechat.miniprogram.service.WechatFaceVerifyService;
import com.mtfm.deadman.plugin.wechat.miniprogram.spi.WechatFaceVerifyHandler;
import com.mtfm.deadman.plugin.wechat.miniprogram.support.WechatFaceVerifyRetSupport;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatFaceVerifyInitiateVO;
import com.mtfm.deadman.plugin.wechat.miniprogram.vo.WechatFaceVerifyQueryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * 用户端微信人脸核身实现，将微信插件与用户端账号体系桥接。
 */
@Component
@RequiredArgsConstructor
public class ClientWechatFaceVerifyHandler implements WechatFaceVerifyHandler {

    private final WechatFaceVerifyService wechatFaceVerifyService;
    private final WechatApiClient wechatApiClient;
    private final ClientUserAccountService clientUserAccountService;

    /**
     * 发起人脸核身，生成业务流水号并调用微信 getVerifyId。
     *
     * @param userId  用户主键
     * @param request 发起请求
     * @return verifyId 及业务流水号
     */
    @Override
    public WechatFaceVerifyInitiateVO initiateVerify(Long userId, WechatFaceVerifyInitiateRequest request) {
        String openid = resolveOpenid(userId, request.code());
        String outSeqNo = generateOutSeqNo();
        WechatFaceCertInfo certInfo = WechatFaceCertInfo.identityCard(
                request.certName().trim(), request.certNo().trim());
        WechatGetVerifyIdResult result = wechatFaceVerifyService.getVerifyId(outSeqNo, certInfo, openid);
        return new WechatFaceVerifyInitiateVO(result.verifyId(), result.expiresIn(), outSeqNo);
    }

    /**
     * 查询人脸核身结果。
     *
     * @param userId  用户主键
     * @param request 查询请求
     * @return 核身结果
     */
    @Override
    public WechatFaceVerifyQueryVO queryVerifyResult(Long userId, WechatFaceVerifyQueryRequest request) {
        String openid = resolveOpenid(userId, request.code());
        WechatFaceCertInfo certInfo = WechatFaceCertInfo.identityCard(
                request.certName().trim(), request.certNo().trim());
        WechatQueryVerifyInfoResult result = wechatFaceVerifyService.queryVerifyInfo(
                request.verifyId().trim(), request.outSeqNo().trim(), certInfo, openid);
        boolean verified = result.verifyRet() == WechatFaceVerifyConstants.VERIFY_RET_SUCCESS;
        return new WechatFaceVerifyQueryVO(
                result.verifyRet(), verified, WechatFaceVerifyRetSupport.describe(result.verifyRet()));
    }

    @Override
    public String loginGroupId() {
        return ClientAuthConstants.LOGIN_GROUP_ID;
    }

    /**
     * 解析用户 openid：优先使用已绑定的微信小程序账号，否则通过 wx.login code 换取。
     *
     * @param userId 用户主键
     * @param code   wx.login 临时凭证
     * @return openid
     */
    private String resolveOpenid(Long userId, String code) {
        if (StringUtils.hasText(code)) {
            return wechatApiClient.code2Session(code.trim()).openid();
        }
        return clientUserAccountService
                .findMiniprogramOpenid(userId)
                .orElseThrow(() -> new BusinessException(
                        ResultCode.BAD_REQUEST, "未绑定微信小程序账号，请先完成微信登录或传入 code"));
    }

    /**
     * 生成符合微信要求的业务流水号（5-32 位字母数字及 _-）。
     *
     * @return 业务流水号
     */
    private String generateOutSeqNo() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
