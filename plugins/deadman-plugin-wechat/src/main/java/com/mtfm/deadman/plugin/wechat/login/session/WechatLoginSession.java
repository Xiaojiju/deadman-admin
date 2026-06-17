package com.mtfm.deadman.plugin.wechat.login.session;

import org.springframework.util.StringUtils;

/**
 * 微信登录会话模板，解析凭证后的标准响应结构。
 * <p>
 * 各登录方式通过实现此接口扩展特有字段（如小程序 sessionKey、网页 accessToken）。
 */
public sealed interface WechatLoginSession
        permits WechatMiniprogramLoginSession, WechatWebLoginSession {

    /**
     * 登录方式标识。
     *
     * @return 登录方式标识
     */
    String loginKind();

    /**
     * OAuth 提供商标识，写入各用户体系 OAuth 账号表。
     *
     * @return OAuth 提供商标识
     */
    String oauthProvider();

    /**
     * 微信 openid。
     *
     * @return openid
     */
    String openid();

    /**
     * 微信 unionid，未绑定时可能为空。
     *
     * @return unionid
     */
    String unionid();

    /**
     * 用户昵称，小程序登录通常为空。
     *
     * @return 昵称
     */
    String nickname();

    /**
     * 用户头像 URL。
     *
     * @return 头像 URL
     */
    String avatarUrl();

    /**
     * 解析 OAuth 主体标识，优先 unionid。
     *
     * @return OAuth subject
     */
    default String oauthSubject() {
        return StringUtils.hasText(unionid()) ? unionid() : openid();
    }

    /**
     * 解析展示昵称。
     *
     * @return 展示昵称
     */
    default String displayNickname() {
        return StringUtils.hasText(nickname()) ? nickname() : "微信用户";
    }
}
