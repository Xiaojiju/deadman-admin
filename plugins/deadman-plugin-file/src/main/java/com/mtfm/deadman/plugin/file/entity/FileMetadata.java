package com.mtfm.deadman.plugin.file.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件元数据，记录上传文件的业务信息与存储引用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("plugin_file_metadata")
public class FileMetadata {

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 对外文件编码，可用于业务关联 */
    private String fileCode;

    /** 原始文件名 */
    private String originalFilename;

    /** MIME 类型 */
    private String contentType;

    /** 文件大小（字节） */
    private Long sizeBytes;

    /** 存储 Provider 标识 */
    private String providerId;

    /** 存储键（相对路径或对象 Key） */
    private String storageKey;

    /** 可直接访问的 URL */
    private String accessUrl;

    /** 业务分类 */
    private String bizType;

    /** 上传人用户 ID */
    private Long uploaderUserId;

    /** 逻辑删除：0-未删除，1-已删除 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
