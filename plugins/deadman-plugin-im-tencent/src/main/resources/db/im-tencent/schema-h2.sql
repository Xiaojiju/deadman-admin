CREATE TABLE IF NOT EXISTS plugin_im_user_account (
    id              BIGINT        NOT NULL,
    realm_id        VARCHAR(32)   NOT NULL,
    subject_id      VARCHAR(64)   NOT NULL,
    im_user_id      VARCHAR(32)   NOT NULL,
    nickname        VARCHAR(64),
    avatar_url      VARCHAR(512),
    status          SMALLINT      NOT NULL DEFAULT 1,
    last_sync_time  TIMESTAMP,
    is_deleted      SMALLINT      NOT NULL DEFAULT 0,
    create_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         INT           NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_plugin_im_realm_subject UNIQUE (realm_id, subject_id, is_deleted),
    CONSTRAINT uk_plugin_im_user_id UNIQUE (im_user_id, is_deleted)
);
