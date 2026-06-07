package com.mtfm.deadman.component.client.auth.handler;

import com.mtfm.deadman.component.client.spi.ClientLoginFailureCallback;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 聚合所有 {@link ClientLoginFailureCallback} 实现，供登录失败时统一回调。
 */
@Component
@RequiredArgsConstructor
public class CompositeClientLoginFailureCallback {

    private final List<ClientLoginFailureCallback> callbacks;

    /**
     * 触发所有已注册的失败回调。
     *
     * @param request    请求
     * @param response   响应
     * @param providerId 提供商标识
     * @param exception  认证异常
     */
    public void onLoginFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            String providerId,
            AuthenticationException exception) {
        for (ClientLoginFailureCallback callback : callbacks) {
            callback.onLoginFailure(request, response, providerId, exception);
        }
    }
}
