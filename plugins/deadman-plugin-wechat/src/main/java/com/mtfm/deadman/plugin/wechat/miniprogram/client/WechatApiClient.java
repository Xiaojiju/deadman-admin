package com.mtfm.deadman.plugin.wechat.miniprogram.client;

import com.mtfm.deadman.plugin.wechat.miniprogram.dto.WechatFaceCertInfo;

/**
 * 微信小程序开放接口客户端。
 */
public interface WechatApiClient {

    /**
     * 使用 wx.login 返回的 code 换取 openid 与 session_key。
     *
     * @param jsCode 临时登录凭证
     * @return 会话信息
     */
    WechatCode2SessionResult code2Session(String jsCode);

    /**
     * 获取接口调用凭据 access_token。
     *
     * @return access_token
     */
    String getAccessToken();

    /**
     * 使用 getPhoneNumber 按钮返回的 code 换取手机号。
     *
     * @param phoneCode 手机号动态令牌
     * @return 手机号信息
     */
    WechatPhoneInfo getPhoneNumber(String phoneCode);

    /**
     * 获取用户人脸核身会话唯一标识 verifyId。
     *
     * @param outSeqNo 业务流水号
     * @param certInfo 用户身份信息
     * @param openid   用户 openid
     * @return verifyId 及有效期
     */
    WechatGetVerifyIdResult getVerifyId(String outSeqNo, WechatFaceCertInfo certInfo, String openid);

    /**
     * 查询用户人脸核身真实验证结果。
     *
     * @param verifyId 人脸核身会话唯一标识
     * @param outSeqNo 业务流水号
     * @param certHash 证件信息摘要
     * @param openid   用户 openid
     * @return 核身结果码
     */
    WechatQueryVerifyInfoResult queryVerifyInfo(String verifyId, String outSeqNo, String certHash, String openid);
}
