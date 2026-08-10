-- 转账姓名落库改为密文 token，加长字段

ALTER TABLE plugin_pay_transfer_batch
    MODIFY COLUMN user_name VARCHAR(512) NULL COMMENT '收款用户姓名（密文 token 或明文兼容）';

ALTER TABLE plugin_pay_transfer_bill
    MODIFY COLUMN user_name VARCHAR(512) NULL COMMENT '收款用户姓名（密文 token 或明文兼容）';
