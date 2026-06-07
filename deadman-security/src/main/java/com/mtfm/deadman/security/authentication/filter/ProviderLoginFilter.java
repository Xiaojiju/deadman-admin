package com.mtfm.deadman.security.authentication.filter;

import com.mtfm.deadman.security.authentication.provider.LoginProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * 通用登录 Filter，每个 {@link LoginProvider} 注册一个实例，endpoint 相互独立。
 */
public class ProviderLoginFilter extends AbstractAuthenticationProcessingFilter {

    private final LoginProvider loginProvider;

    /**
     * 创建 Provider 专属登录 Filter。
     *
     * @param loginProvider  登录 Provider
     * @param loginEndpoint  完整登录路径
     * @param authManager    认证管理器
     * @param successHandler 成功处理器
     * @param failureHandler 失败处理器
     */
    public ProviderLoginFilter(
            LoginProvider loginProvider,
            String loginEndpoint,
            AuthenticationManager authManager,
            AuthenticationSuccessHandler successHandler,
            AuthenticationFailureHandler failureHandler) {
        super(PathPatternRequestMatcher.withDefaults().matcher(loginEndpoint));
        this.loginProvider = loginProvider;
        setAuthenticationManager(authManager);
        setAuthenticationSuccessHandler(successHandler);
        setAuthenticationFailureHandler(failureHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        Authentication authRequest = loginProvider.createAuthenticationRequest(request);
        return getAuthenticationManager().authenticate(authRequest);
    }
}
