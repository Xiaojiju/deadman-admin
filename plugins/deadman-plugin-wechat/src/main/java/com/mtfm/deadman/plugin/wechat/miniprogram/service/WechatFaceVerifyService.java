package com.mtfm.deadman.plugin.wechat.miniprogram.service;

import com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatApiClient;
import com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatGetVerifyIdResult;
import com.mtfm.deadman.plugin.wechat.miniprogram.client.WechatQueryVerifyInfoResult;
import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatFaceCertInfo;
import com.mtfm.deadman.plugin.wechat.miniprogram.support.WechatFaceCertHashSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 微信小程序人脸核身服务，封装 getVerifyId 与 queryVerifyInfo 调用。
 */
@Service
@RequiredArgsConstructor
public class WechatFaceVerifyService {

    private final WechatApiClient wechatApiClient;

    /**
     * 获取人脸核身会话唯一标识 verifyId。
     *
     * @param outSeqNo 业务流水号，同一 appid 下唯一
     * @param certInfo 用户身份信息
     * @param openid   用户 openid，须与前端 wx.requestFacialVerify 一致
     * @return verifyId 及有效期
     */
    public WechatGetVerifyIdResult getVerifyId(String outSeqNo, WechatFaceCertInfo certInfo, String openid) {
        return wechatApiClient.getVerifyId(outSeqNo, certInfo, openid);
    }

    /**
     * 查询人脸核身真实验证结果。
     *
     * @param verifyId 发起核身时返回的 verifyId
     * @param outSeqNo 业务流水号，须与 getVerifyId 一致
     * @param certInfo 用户身份信息，须与 getVerifyId 一致
     * @param openid   用户 openid，须与 getVerifyId 一致
     * @return 核身结果码 verifyRet
     */
    public WechatQueryVerifyInfoResult queryVerifyInfo(
            String verifyId, String outSeqNo, WechatFaceCertInfo certInfo, String openid) {
        String certHash = WechatFaceCertHashSupport.computeCertHash(certInfo);
        return wechatApiClient.queryVerifyInfo(verifyId, outSeqNo, certHash, openid);
    }
}
