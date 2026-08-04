package com.mtfm.deadman.component.client.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户端站内信收件记录。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("client_notification_recipient")
public class ClientNotificationRecipient {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 通知主键 */
    private Long notificationId;

    /** 收件人用户 ID */
    private Long userId;

    /** 阅读状态：0-未读，1-已读 */
    private Integer readStatus;

    /** 阅读时间 */
    private LocalDateTime readTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
