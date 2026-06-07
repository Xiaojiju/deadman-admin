package com.mtfm.deadman.plugin.wechat.miniprogram.client;

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
}
