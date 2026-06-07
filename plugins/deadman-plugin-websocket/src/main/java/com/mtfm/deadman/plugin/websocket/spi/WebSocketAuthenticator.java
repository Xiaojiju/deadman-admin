package com.mtfm.deadman.plugin.websocket.spi;

import org.springframework.http.server.ServerHttpRequest;

import java.util.Optional;

/**
 * WebSocket 握手鉴权 SPI，由业务模块按通道实现（如 admin 走 JWT，mobile 走独立 token）。
 */
public interface WebSocketAuthenticator {

    /**
     * 是否支持该通道。
     *
     * @param channelCode 通道编码
     * @return 是否支持
     */
    boolean supports(String channelCode);

    /**
     * 握手鉴权并解析通道内用户标识。
     *
     * @param channelCode 通道编码
     * @param request     握手请求
     * @return 鉴权成功时返回 principal
     */
    Optional<WebSocketPrincipal> authenticate(String channelCode, ServerHttpRequest request);
}
