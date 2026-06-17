package com.mtfm.deadman.plugin.wechat.login;

import com.mtfm.deadman.plugin.wechat.miniprogram.WechatMiniprogramConstants;
import com.mtfm.deadman.plugin.wechat.web.WechatWebConstants;

/**
 * 微信登录方式标识常量。
 */
public final class WechatLoginKinds {

    /** 微信小程序登录 */
    public static final String MINIPROGRAM = WechatMiniprogramConstants.OAUTH_PROVIDER;

    /** 微信网页扫码登录 */
    public static final String WEB = WechatWebConstants.OAUTH_PROVIDER;

    private WechatLoginKinds() {
    }
}
