package com.mtfm.deadman.notification.config;

import com.mtfm.deadman.notification.service.NotificationPushService;
import com.mtfm.deadman.plugin.websocket.channel.MessageChannel;
import com.mtfm.deadman.plugin.websocket.spi.WebSocketAuthenticator;
import com.mtfm.deadman.plugin.websocket.spi.WebSocketPrincipal;
import com.mtfm.deadman.plugin.websocket.spi.WebSocketPrincipalAttributes;
import com.mtfm.deadman.security.jwt.RealmJwtTokenProvider;
import com.mtfm.deadman.security.token.AuthTokenIssueProviderRegistry;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 用户端站内信 WebSocket 握手鉴权：query {@code token} 携带 CLIENT 域 JWT。
 */
@Slf4j
@Component
public class ClientInboxWebSocketAuthenticator implements WebSocketAuthenticator {

    private static final String TOKEN_PARAM = "token";
    /** 与 {@code ClientAuthConstants.JWT_REALM} 对齐，避免 notification 模块依赖 client 组件 */
    private static final String CLIENT_JWT_REALM = "CLIENT";

    private final AuthTokenIssueProviderRegistry providerRegistry;
    private final MessageChannel clientInboxMessageChannel;

    public ClientInboxWebSocketAuthenticator(
            AuthTokenIssueProviderRegistry providerRegistry,
            @Qualifier(NotificationPushService.CLIENT_INBOX_MESSAGE_CHANNEL) MessageChannel clientInboxMessageChannel) {
        this.providerRegistry = providerRegistry;
        this.clientInboxMessageChannel = clientInboxMessageChannel;
    }

    /**
     * 是否处理用户端站内信通道。
     *
     * @param channelCode 通道编码
     * @return 是否为本通道
     */
    @Override
    public boolean supports(String channelCode) {
        return clientInboxMessageChannel.getCode().equals(channelCode);
    }

    /**
     * 用 CLIENT 域 JWT 完成握手鉴权，userKey 为用户端用户 ID。
     *
     * @param channelCode 通道编码
     * @param request     握手请求
     * @return 鉴权成功时的主体
     */
    @Override
    public Optional<WebSocketPrincipal> authenticate(String channelCode, ServerHttpRequest request) {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }
        try {
            RealmJwtTokenProvider jwtTokenProvider = providerRegistry
                    .require(CLIENT_JWT_REALM)
                    .jwtSupport()
                    .tokenProvider();
            Claims claims = jwtTokenProvider.parseClaims(token);
            Long userId = jwtTokenProvider.getUserId(claims);
            String userCode = jwtTokenProvider.getUserCode(claims);
            return Optional.of(new WebSocketPrincipal(
                    channelCode,
                    String.valueOf(userId),
                    new WebSocketPrincipalAttributes(userCode != null ? userCode : "")));
        } catch (JwtException | IllegalArgumentException | IllegalStateException ex) {
            log.debug("用户端站内信 WebSocket 鉴权失败: {}", ex.getMessage());
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
