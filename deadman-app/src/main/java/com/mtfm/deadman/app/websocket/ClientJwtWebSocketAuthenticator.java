package com.mtfm.deadman.app.websocket;

import com.mtfm.deadman.component.client.constants.ClientAuthConstants;
import com.mtfm.deadman.plugin.websocket.spi.WebSocketAuthenticator;
import com.mtfm.deadman.plugin.websocket.spi.WebSocketPrincipal;
import com.mtfm.deadman.plugin.websocket.spi.WebSocketPrincipalAttributes;
import com.mtfm.deadman.security.jwt.RealmJwtTokenProvider;
import com.mtfm.deadman.security.token.AuthTokenIssueProviderRegistry;
import com.mtfm.deadman.plugin.websocket.channel.MessageChannel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 用户端 WebSocket 握手鉴权：通过 query {@code token} 携带 CLIENT 域 JWT，userKey 为用户端用户 ID。
 */
@Component
public class ClientJwtWebSocketAuthenticator implements WebSocketAuthenticator {

    private static final Logger log = LoggerFactory.getLogger(ClientJwtWebSocketAuthenticator.class);
    private static final String TOKEN_PARAM = "token";

    private final AuthTokenIssueProviderRegistry providerRegistry;
    private final MessageChannel mobileMessageChannel;

    public ClientJwtWebSocketAuthenticator(
        AuthTokenIssueProviderRegistry providerRegistry,
        @Qualifier(WebSocketChannelConfiguration.MOBILE_MESSAGE_CHANNEL) MessageChannel mobileMessageChannel) {
        this.providerRegistry = providerRegistry;
        this.mobileMessageChannel = mobileMessageChannel;
    }

    @Override
    public boolean supports(String channelCode) {
        return mobileMessageChannel.getCode().equals(channelCode);
    }

    @Override
    public Optional<WebSocketPrincipal> authenticate(String channelCode, ServerHttpRequest request) {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }
        try {
            RealmJwtTokenProvider jwtTokenProvider = providerRegistry.require(ClientAuthConstants.JWT_REALM).jwtSupport()
                .tokenProvider();
            Claims claims = jwtTokenProvider.parseClaims(token);
            Long userId = jwtTokenProvider.getUserId(claims);
            String userCode = jwtTokenProvider.getUserCode(claims);
            return Optional.of(new WebSocketPrincipal(channelCode, String.valueOf(userId),
                new WebSocketPrincipalAttributes(userCode != null ? userCode : "")));
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("WebSocket client 鉴权失败: {}", ex.getMessage());
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
