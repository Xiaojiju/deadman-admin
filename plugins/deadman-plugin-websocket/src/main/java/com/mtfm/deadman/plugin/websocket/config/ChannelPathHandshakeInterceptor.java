package com.mtfm.deadman.plugin.websocket.config;

import com.mtfm.deadman.plugin.websocket.session.WebSocketHandshakeInterceptor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * 将 URL 路径中的通道编码写入请求属性，供鉴权拦截器使用。
 */
public class ChannelPathHandshakeInterceptor implements HandshakeInterceptor {

    private final String channelCode;

    public ChannelPathHandshakeInterceptor(String channelCode) {
        this.channelCode = channelCode;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            servletRequest.getServletRequest().setAttribute(WebSocketHandshakeInterceptor.ATTR_CHANNEL_CODE, channelCode);
        }
        attributes.put(WebSocketHandshakeInterceptor.ATTR_CHANNEL_CODE, channelCode);
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
    }
}
