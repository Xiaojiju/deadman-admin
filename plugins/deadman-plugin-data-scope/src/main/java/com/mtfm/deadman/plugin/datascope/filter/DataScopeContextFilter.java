package com.mtfm.deadman.plugin.datascope.filter;

import com.mtfm.deadman.common.spi.DataScopeAuthPrincipal;
import com.mtfm.deadman.plugin.datascope.context.DataScopeRequestContextHolder;
import com.mtfm.deadman.plugin.datascope.model.DataScopeUserContext;
import com.mtfm.deadman.plugin.datascope.service.DataScopeSessionCache;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 认证成功后从缓存加载数据权限上下文并注入 {@link DataScopeRequestContextHolder}。
 */
@Component
@RequiredArgsConstructor
public class DataScopeContextFilter extends OncePerRequestFilter {

    private final DataScopeSessionCache sessionCache;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            injectContextIfAuthenticated();
            filterChain.doFilter(request, response);
        } finally {
            DataScopeRequestContextHolder.clear();
        }
    }

    private void injectContextIfAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof DataScopeAuthPrincipal principal)) {
            return;
        }
        if (principal.superAdmin()) {
            DataScopeRequestContextHolder.set(DataScopeUserContext.bypass(principal.userId()));
            return;
        }
        DataScopeUserContext context = sessionCache.get(principal.userId());
        DataScopeRequestContextHolder.set(context);
    }
}
