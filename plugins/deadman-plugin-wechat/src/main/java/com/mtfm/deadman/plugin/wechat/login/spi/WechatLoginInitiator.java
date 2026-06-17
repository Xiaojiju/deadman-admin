package com.mtfm.deadman.plugin.wechat.login.spi;

import com.mtfm.deadman.plugin.wechat.login.initiate.WechatLoginInitiateResult;

/**
 * 微信登录发起 SPI，用于需要预生成登录参数的场景（如网页扫码）。
 */
public interface WechatLoginInitiator {

    /**
     * 支持的登录方式标识。
     *
     * @return 登录方式标识
     */
    String loginKind();

    /**
     * 发起登录流程并返回方式特有参数。
     *
     * @return 发起结果
     */
    WechatLoginInitiateResult initiate();
}
