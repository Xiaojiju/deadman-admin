-- deadman-plugin-file 文件元数据表（MySQL 8+）

CREATE TABLE IF NOT EXISTS plugin_file_metadata (
    id                  BIGINT        NOT NULL COMMENT '主键',
    file_code           VARCHAR(64)   NOT NULL COMMENT '对外文件编码',
    original_filename   VARCHAR(512)  NOT NULL COMMENT '原始文件名',
    content_type        VARCHAR(128)           COMMENT 'MIME 类型',
    size_bytes          BIGINT        NOT NULL COMMENT '文件大小（字节）',
    provider_id         VARCHAR(32)   NOT NULL COMMENT '存储 Provider 标识',
    storage_key         VARCHAR(1024) NOT NULL COMMENT '存储键（相对路径或对象 Key）',
    access_url          VARCHAR(1024)          COMMENT '可直接访问的 URL',
    biz_type            VARCHAR(64)            COMMENT '业务分类',
    uploader_user_id    BIGINT                 COMMENT '上传人用户 ID',
    is_deleted          SMALLINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_plugin_file_metadata_code (file_code),
    KEY idx_plugin_file_metadata_uploader (uploader_user_id, is_deleted),
    KEY idx_plugin_file_metadata_biz (biz_type, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件元数据';
