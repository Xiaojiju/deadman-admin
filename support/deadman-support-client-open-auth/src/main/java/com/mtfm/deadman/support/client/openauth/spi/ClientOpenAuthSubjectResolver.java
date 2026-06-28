package com.mtfm.deadman.support.client.openauth.spi;

import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.component.openauth.spi.OpenAuthSubject;
import com.mtfm.deadman.component.openauth.spi.OpenAuthSubjectResolver;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * 用户端开放授权主体解析器。
 */
@Component
public class ClientOpenAuthSubjectResolver implements OpenAuthSubjectResolver {

    /**
     * 用户端域标识。
     *
     * @return client
     */
    @Override
    public String realmId() {
        return ClientAuthConstants.LOGIN_GROUP_ID;
    }

    /**
     * 是否为用户端登录用户。
     *
     * @param authentication 认证对象
     * @return 是否支持
     */
    @Override
    public boolean supports(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof ClientLoginUser;
    }

    /**
     * 解析用户端登录用户为主体。
     *
     * @param authentication 认证对象
     * @return 授权主体
     */
    @Override
    public OpenAuthSubject resolve(Authentication authentication) {
        ClientLoginUser loginUser = (ClientLoginUser) authentication.getPrincipal();
        return new OpenAuthSubject("client_user", loginUser.getUserId(), loginUser.getUserCode());
    }
}
