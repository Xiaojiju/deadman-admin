package com.mtfm.deadman.plugin.websocket.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket 统一消息负载体，各通道可继承并扩展字段或向 {@link #payload} 写入通道特有数据。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WsMessage {

    /** 消息唯一 ID */
    private String messageId;

    /** 消息通道编码，用于隔离不同用户体系 */
    private String channelCode;

    /** 消息类型，业务自定义 */
    private String messageType;

    /** 目标用户在通道内的标识（与握手鉴权解析出的 userKey 一致） */
    private String targetUserKey;

    /** 扩展负载，通道特有字段可放于此 */
    @Builder.Default
    private Map<String, Object> payload = new HashMap<>();

    /** 消息创建时间 */
    private LocalDateTime createTime;
}
