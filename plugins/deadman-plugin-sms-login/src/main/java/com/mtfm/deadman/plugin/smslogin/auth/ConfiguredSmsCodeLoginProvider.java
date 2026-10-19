package com.mtfm.deadman.plugin.smslogin.auth;

import java.io.IOException;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.plugin.sms.SmsCodeService;
import com.mtfm.deadman.plugin.sms.SmsScenes;
import com.mtfm.deadman.plugin.smslogin.SmsLoginMessages;
import com.mtfm.deadman.plugin.smslogin.config.SmsLoginBinding;
import com.mtfm.deadman.plugin.smslogin.dto.SmsLoginRequest;
import com.mtfm.deadman.plugin.smslogin.spi.SmsLoginUserLoader;
import com.mtfm.deadman.security.authentication.provider.LoginProvider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

/**
 * 按绑定配置实例化的短信验证码登录 Provider。用户开通由对应组的 {@link SmsLoginUserLoader} 完成。
 */
@RequiredArgsConstructor
public class ConfiguredSmsCodeLoginProvider implements LoginProvider {

    private static final String PROVIDER_ID = "sms";

    private final SmsLoginBinding binding;
    private final SmsCodeService smsCodeService;
    private final ObjectProvider<SmsLoginUserLoader> loaders;
    private final JsonMapper jsonMapper;

    /**
     * 所属登录组。
     *
     * @return 组标识
     */
    @Override
    public String loginGroupId() {
        return binding.groupId();
    }

    /**
     * 提供商标识。
     *
     * @return sms
     */
    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    /**
     * 登录路径段。
     *
     * @return 路径段
     */
    @Override
    public String loginPathSegment() {
        if (binding.loginPathSegment() != null && !binding.loginPathSegment().isBlank()) {
            return binding.loginPathSegment();
        }
        return PROVIDER_ID;
    }

    /**
     * 从请求体读取手机号与验证码。
     *
     * @param request 登录请求
     * @return 未认证令牌
     */
    @Override
    public Authentication createAuthenticationRequest(HttpServletRequest request) throws AuthenticationException {
        SmsLoginRequest loginRequest = parse(request);
        if (loginRequest == null || !StringUtils.hasText(loginRequest.phone())
            || !StringUtils.hasText(loginRequest.code())) {
            throw new AuthenticationServiceException(
                SmsLoginMessages.text("sms.login.credentials_required", "手机号或验证码不能为空"));
        }
        return new SmsCodeAuthenticationToken(loginRequest.phone().trim(), loginRequest.code().trim());
    }

    /**
     * 是否处理短信令牌。
     *
     * @param authentication 认证类型
     * @return 是否支持
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return SmsCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 校验验证码后交给用户加载器签发已认证用户。
     *
     * @param authentication 未认证令牌
     * @return 已认证令牌
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String phone = authentication.getName();
        String code = authentication.getCredentials() == null ? null : authentication.getCredentials().toString();
        try {
            smsCodeService.consume(SmsScenes.LOGIN, phone, code);
        } catch (BusinessException ex) {
            throw new BadCredentialsException(ex.resolveMessage(), ex);
        }
        SmsLoginUserLoader loader = loaders.orderedStream()
            .filter(candidate -> loginGroupId().equals(candidate.loginGroupId())).findFirst().orElse(null);
        if (loader == null) {
            throw new AuthenticationServiceException(
                SmsLoginMessages.text("sms.login.loader_missing", "未配置该用户体系的短信登录用户加载器"));
        }
        try {
            return loader.loadAuthenticatedUser(phone);
        } catch (BusinessException ex) {
            throw new BadCredentialsException(ex.resolveMessage(), ex);
        }
    }

    private SmsLoginRequest parse(HttpServletRequest request) {
        try {
            return jsonMapper.readValue(request.getInputStream(), SmsLoginRequest.class);
        } catch (IOException ex) {
            throw new AuthenticationServiceException(SmsLoginMessages.text("sms.login.parse_failed", "登录请求解析失败"), ex);
        }
    }
}
