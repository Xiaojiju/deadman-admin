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
 * 用户端站内信主记录。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("client_notification")
public class ClientNotification {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 标题 */
    private String title;

    /** 正文 */
    private String content;

    /** 业务类型（如 banner_start） */
    private String bizType;

    /** 业务主键（如轮播图 ID） */
    private Long bizId;

    /** 扩展载荷 JSON */
    private String extraJson;

    /** 实际投递用户数 */
    private Integer recipientCount;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
