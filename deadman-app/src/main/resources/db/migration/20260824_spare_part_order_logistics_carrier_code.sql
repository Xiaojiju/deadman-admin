-- 备件订单发货：记录平台统一快递公司编码，供快递100等渠道查单
ALTER TABLE spare_part_order
    ADD COLUMN logistics_carrier_code VARCHAR(32) NULL COMMENT '物流公司统一编码（如 YTO/SF）' AFTER shipping_address_json;
