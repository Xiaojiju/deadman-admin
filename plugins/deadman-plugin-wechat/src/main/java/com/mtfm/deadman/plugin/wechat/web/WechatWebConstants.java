package com.mtfm.deadman.plugin.wechat.web;

/**
 * 微信网页扫码登录（开放平台网站应用）常量。
 */
public final class WechatWebConstants {

    /** OAuth 提供商标识，写入各用户体系 OAuth 账号表 */
    public static final String OAUTH_PROVIDER = "wechat-web";

    /** 登录 Provider 路径段 */
    public static final String LOGIN_PATH_SEGMENT = "wechat-web";

    /** 微信开放平台扫码登录页基础地址 */
    public static final String QR_CONNECT_BASE_URL = "https://open.weixin.qq.com/connect/qrconnect";

    /** 插件级扫码授权地址 API 路径（公开） */
    public static final String AUTHORIZE_URL_API_PATH = "/api/wechat-web/authorize-url";

    private WechatWebConstants() {
    }
}
