-- IM 头像：增加 fileId 稳定键，并加长 FaceUrl 快照以容纳签名 URL
ALTER TABLE plugin_im_user_account
    ADD COLUMN avatar_file_id BIGINT NULL COMMENT '头像文件 ID 快照（稳定比对键）' AFTER nickname;

ALTER TABLE plugin_im_user_account
    MODIFY COLUMN avatar_url VARCHAR(2048) NULL COMMENT '头像 URL 快照（FaceUrl）';
