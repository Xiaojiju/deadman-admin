package com.mtfm.deadman.security.authentication.provider;

import com.mtfm.deadman.security.constants.AdminAuthConstants;
import org.springframework.stereotype.Component;

/**
 * 管理端登录 Provider 组贡献者。
 */
@Component
public class AdminLoginProviderGroupContributor implements LoginProviderGroupContributor {

    @Override
    public LoginProviderGroup group() {
        return new LoginProviderGroup(
                AdminAuthConstants.LOGIN_GROUP_ID, "/api", "/api/auth", "");
    }
}
