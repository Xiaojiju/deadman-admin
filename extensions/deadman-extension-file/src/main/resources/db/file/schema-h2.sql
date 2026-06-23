-- deadman-extension-file 集成测试用 H2（MySQL 兼容模式）

CREATE TABLE IF NOT EXISTS plugin_file_metadata (
    id                  BIGINT        NOT NULL,
    file_code           VARCHAR(64)   NOT NULL,
    original_filename   VARCHAR(512)  NOT NULL,
    content_type        VARCHAR(128),
    size_bytes          BIGINT        NOT NULL,
    provider_id         VARCHAR(32)   NOT NULL,
    storage_key         VARCHAR(1024) NOT NULL,
    storage_bucket      VARCHAR(128),
    access_url          VARCHAR(1024),
    biz_type            VARCHAR(64),
    uploader_user_id    BIGINT,
    is_deleted          SMALLINT      NOT NULL DEFAULT 0,
    create_time         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_plugin_file_metadata_code UNIQUE (file_code)
);
