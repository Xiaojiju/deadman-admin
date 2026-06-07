package com.mtfm.deadman.security.authentication.provider;

/**
 * 登录 Provider 组贡献 SPI，各用户体系模块注册自己的 endpoint 前缀绑定。
 */
public interface LoginProviderGroupContributor {

    /**
     * 贡献一个 Provider 组描述。
     *
     * @return Provider 组
     */
    LoginProviderGroup group();
}
