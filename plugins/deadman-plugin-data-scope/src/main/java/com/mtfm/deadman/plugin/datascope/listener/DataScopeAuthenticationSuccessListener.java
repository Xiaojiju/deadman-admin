package com.mtfm.deadman.plugin.datascope.listener;

import com.mtfm.deadman.common.spi.DataScopeAuthPrincipal;
import com.mtfm.deadman.plugin.datascope.service.DataScopeSessionCache;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * 登录成功后预热数据权限运行时缓存。
 */
@Component
@RequiredArgsConstructor
public class DataScopeAuthenticationSuccessListener {

    private final DataScopeSessionCache sessionCache;

    /**
     * 认证成功后刷新非超管用户的数据权限缓存。
     *
     * @param event Spring Security 认证成功事件
     */
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        if (!(event.getAuthentication().getPrincipal() instanceof DataScopeAuthPrincipal principal)) {
            return;
        }
        if (principal.superAdmin()) {
            return;
        }
        sessionCache.refresh(principal.userId());
    }
}
