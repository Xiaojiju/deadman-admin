package com.mtfm.deadman.app.websocket;

import com.mtfm.deadman.plugin.websocket.channel.MessageChannel;
import com.mtfm.deadman.plugin.websocket.spi.WebSocketAuthenticator;
import com.mtfm.deadman.plugin.websocket.spi.WebSocketPrincipal;
import com.mtfm.deadman.security.constants.AdminAuthConstants;
import com.mtfm.deadman.security.jwt.RealmJwtTokenProvider;
import com.mtfm.deadman.security.token.AuthTokenIssueProviderRegistry;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mtfm.deadman.plugin.websocket.spi.WebSocketPrincipalAttributes;

import java.util.Optional;

/**
 * 管理端 WebSocket 握手鉴权：通过 query {@code token} 携带 JWT，userKey 为系统用户 ID。
 */
@Component
public class AdminJwtWebSocketAuthenticator implements WebSocketAuthenticator {

    private static final Logger log = LoggerFactory.getLogger(AdminJwtWebSocketAuthenticator.class);
    private static final String TOKEN_PARAM = "token";

    private final AuthTokenIssueProviderRegistry providerRegistry;
    private final MessageChannel adminMessageChannel;

    public AdminJwtWebSocketAuthenticator(
            AuthTokenIssueProviderRegistry providerRegistry,
            @Qualifier(WebSocketChannelConfiguration.ADMIN_MESSAGE_CHANNEL) MessageChannel adminMessageChannel) {
        this.providerRegistry = providerRegistry;
        this.adminMessageChannel = adminMessageChannel;
    }

    @Override
    public boolean supports(String channelCode) {
        return adminMessageChannel.getCode().equals(channelCode);
    }

    @Override
    public Optional<WebSocketPrincipal> authenticate(String channelCode, ServerHttpRequest request) {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }
        try {
            RealmJwtTokenProvider jwtTokenProvider = providerRegistry
                    .require(AdminAuthConstants.JWT_REALM)
                    .jwtSupport()
                    .tokenProvider();
            Claims claims = jwtTokenProvider.parseClaims(token);
            Long userId = jwtTokenProvider.getUserId(claims);
            String userCode = jwtTokenProvider.getUserCode(claims);
            return Optional.of(new WebSocketPrincipal(
                    channelCode,
                    String.valueOf(userId),
                    new WebSocketPrincipalAttributes(userCode != null ? userCode : "")));
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("WebSocket admin 鉴权失败: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private String resolveToken(ServerHttpRequest request) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return null;
        }
        return servletRequest.getServletRequest().getParameter(TOKEN_PARAM);
    }
}
