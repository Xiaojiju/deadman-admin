package com.mtfm.deadman.plugin.websocket.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WebSocket 消息持久化记录。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("plugin_ws_message")
public class WsMessageRecord {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 业务消息 ID，全局唯一 */
    private String messageId;

    /** 消息通道编码 */
    private String channelCode;

    /** 消息类型 */
    private String messageType;

    /** 目标用户标识 */
    private String targetUserKey;

    /** JSON 负载 */
    private String payloadJson;

    /** 投递状态，参见 {@link com.mtfm.deadman.plugin.websocket.message.MessageSendStatus} */
    private Integer status;

    /** 已重试次数 */
    private Integer retryCount;

    /** 最大重试次数 */
    private Integer maxRetry;

    /** 下次重试时间 */
    private LocalDateTime nextRetryTime;

    /** 最近一次失败原因 */
    private String errorMessage;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 乐观锁 */
    @Version
    private Integer version;
}
