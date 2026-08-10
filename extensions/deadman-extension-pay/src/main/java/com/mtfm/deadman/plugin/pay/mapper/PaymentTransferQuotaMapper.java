package com.mtfm.deadman.plugin.pay.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtfm.deadman.plugin.pay.entity.PaymentTransferQuota;

/**
 * 商家转账额度配置 Mapper。
 */
@Mapper
public interface PaymentTransferQuotaMapper extends BaseMapper<PaymentTransferQuota> {

    /**
     * 锁定当前额度配置行（派发抢占时串行化额度校验）。
     *
     * @return 额度配置，不存在时为 null
     */
    @Select("""
            SELECT *
            FROM plugin_pay_transfer_quota
            WHERE is_deleted = 0
            ORDER BY id ASC
            LIMIT 1
            FOR UPDATE
            """)
    PaymentTransferQuota selectCurrentForUpdate();
}
