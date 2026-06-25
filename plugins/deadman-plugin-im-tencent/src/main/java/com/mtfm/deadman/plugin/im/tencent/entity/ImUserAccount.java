package com.mtfm.deadman.plugin.im.tencent.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IM 用户映射表，维护业务主体与腾讯云 UserID 的关系。
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("plugin_im_user_account")
public class ImUserAccount {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户域标识 */
    private String realmId;

    /** 域内稳定主键 */
    private String subjectId;

    /** 腾讯云 IM UserID */
    private String imUserId;

    /** 昵称快照 */
    private String nickname;

    /** 头像 URL 快照 */
    private String avatarUrl;

    /** 状态：1-正常，0-禁用 */
    private Integer status;

    /** 最近一次同步到腾讯云的时间 */
    private LocalDateTime lastSyncTime;

    /** 逻辑删除：0-未删除，1-已删除 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 乐观锁版本号 */
    @Version
    private Integer version;
}
