package com.mtfm.deadman.component.client.config;

import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.security.authentication.provider.LoginProviderGroup;
import com.mtfm.deadman.security.authentication.provider.LoginProviderGroupContributor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用户端登录 Provider 组贡献者，绑定 /client/api endpoint 前缀。
 */
@Component
@RequiredArgsConstructor
public class ClientLoginProviderGroupContributor implements LoginProviderGroupContributor {

    private final ClientComponentProperties clientComponentProperties;

    @Override
    public LoginProviderGroup group() {
        return new LoginProviderGroup(
                ClientAuthConstants.LOGIN_GROUP_ID,
                "/client/api",
                clientComponentProperties.getAuth().getBasePath(),
                clientComponentProperties.getAuth().getLoginPathPrefix());
    }
}
