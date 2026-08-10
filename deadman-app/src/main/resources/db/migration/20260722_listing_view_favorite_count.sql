-- 工程信息发布门面：浏览次数 / 收藏次数
ALTER TABLE engineering_listing
    ADD COLUMN view_count BIGINT NOT NULL DEFAULT 0 COMMENT '浏览次数' AFTER list_time,
    ADD COLUMN favorite_count BIGINT NOT NULL DEFAULT 0 COMMENT '收藏次数' AFTER view_count;
