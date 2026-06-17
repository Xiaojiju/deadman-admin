package com.mtfm.deadman.plugin.wechat.login.spi;

import com.mtfm.deadman.plugin.wechat.login.credential.WechatLoginCredential;
import com.mtfm.deadman.plugin.wechat.login.session.WechatLoginSession;

/**
 * 微信登录解析 SPI，每种登录方式实现一个 Bean。
 */
public interface WechatLoginResolver {

    /**
     * 支持的登录方式标识。
     *
     * @return 登录方式标识
     */
    String loginKind();

    /**
     * 是否支持该凭证类型。
     *
     * @param credential 登录凭证
     * @return 是否支持
     */
    boolean supports(WechatLoginCredential credential);

    /**
     * 将登录凭证解析为统一会话模板。
     *
     * @param credential 登录凭证
     * @return 登录会话
     */
    WechatLoginSession resolve(WechatLoginCredential credential);
}
