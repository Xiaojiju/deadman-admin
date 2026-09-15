-- 平台保险产品（仅后台发布，与工程信息 listing 门面解耦）
CREATE TABLE IF NOT EXISTS engineering_insurance_product (
    id                  BIGINT         NOT NULL COMMENT '主键',
    title               VARCHAR(128)   NOT NULL COMMENT '标题',
    cover_file_id       BIGINT         NOT NULL COMMENT '封面图文件 ID',
    image_file_ids      VARCHAR(2048)  NULL COMMENT '图文详情图片文件 ID 列表（JSON 数组）',
    estimated_price     DECIMAL(12, 2) NULL COMMENT '预估价（元），可空表示面议',
    content             MEDIUMTEXT     NULL COMMENT '图文详情（富文本/HTML）',
    contact_phone       VARCHAR(32)    NULL COMMENT '联系电话',
    wechat_service_url  VARCHAR(512)   NULL COMMENT '微信客服链接（企微/客服）',
    sort_order          INT            NOT NULL DEFAULT 0 COMMENT '排序号（升序）',
    status              TINYINT        NOT NULL DEFAULT 0 COMMENT '状态：0-下架，1-上架',
    create_time         DATETIME       NOT NULL COMMENT '创建时间',
    update_time         DATETIME       NOT NULL COMMENT '更新时间',
    is_deleted          TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    version             INT            NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (id),
    KEY idx_eng_insurance_product_status_sort (status, sort_order, is_deleted)
) COMMENT '平台保险产品（仅后台发布）';
