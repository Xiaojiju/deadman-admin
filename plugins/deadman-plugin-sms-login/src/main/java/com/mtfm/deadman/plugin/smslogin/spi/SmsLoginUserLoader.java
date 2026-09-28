package com.mtfm.deadman.plugin.smslogin.spi;

import org.springframework.security.core.Authentication;

/**
 * 验证码消费成功后，由具体用户体系加载或开通登录用户。
 */
public interface SmsLoginUserLoader {

    /**
     * 所属登录组，与 {@code login-bindings.group-id} 对应。
     *
     * @return 组标识
     */
    String loginGroupId();

    /**
     * 按手机号返回已认证令牌。手机号尚未注册时可由实现自行开通。
     *
     * @param phone 已规范化的手机号
     * @return 已认证令牌
     */
    Authentication loadAuthenticatedUser(String phone);
}
