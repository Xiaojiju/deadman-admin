package com.mtfm.deadman.notification.entity;

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
 * 站内信收件人记录（用户收件箱持久化）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_notification_recipient")
public class SysNotificationRecipient {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 通知主键 */
    private Long notificationId;

    /** 收件人用户 ID */
    private Long userId;

    /** 阅读状态，参见 {@link com.mtfm.deadman.notification.enums.NotificationReadStatus} */
    private Integer readStatus;

    /** 阅读时间 */
    private LocalDateTime readTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
