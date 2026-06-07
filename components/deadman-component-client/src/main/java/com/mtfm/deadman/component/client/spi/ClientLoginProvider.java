package com.mtfm.deadman.component.client.spi;

import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.security.authentication.provider.LoginProvider;

/**
 * 用户端登录 Provider SPI，继承 security 统一 {@link LoginProvider}，固定归属 client 组。
 * <p>
 * 各 Provider 拥有独立 endpoint，由 {@link com.mtfm.deadman.security.authentication.provider.LoginProviderGroupManager} 统一注册 Filter。
 */
public interface ClientLoginProvider extends LoginProvider {

    /**
     * 固定归属用户端 Provider 组。
     *
     * @return client 组标识
     */
    @Override
    default String loginGroupId() {
        return ClientAuthConstants.LOGIN_GROUP_ID;
    }
}
