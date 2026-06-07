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
 * 站内信通知主记录。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_notification")
public class SysNotification {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 标题 */
    private String title;

    /** 正文 */
    private String content;

    /** 目标类型，参见 {@link com.mtfm.deadman.notification.enums.NotificationTargetType} */
    private Integer targetType;

    /** 目标参数 JSON，如 userIds、departmentIds */
    private String targetPayloadJson;

    /** 发送人用户 ID */
    private Long senderUserId;

    /** 实际投递用户数 */
    private Integer recipientCount;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
