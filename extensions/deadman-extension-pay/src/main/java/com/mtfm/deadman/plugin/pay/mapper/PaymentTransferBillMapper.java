package com.mtfm.deadman.plugin.pay.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtfm.deadman.plugin.pay.entity.PaymentTransferBill;

/**
 * 商家转账明细 Mapper。
 */
@Mapper
public interface PaymentTransferBillMapper extends BaseMapper<PaymentTransferBill> {

    /**
     * 列出可派发待转（排除已停止批次后再 LIMIT，避免 STOPPED 占满窗口导致饿死）。
     *
     * @param limit 最大条数
     * @return 待转明细
     */
    @Select("""
            SELECT b.*
            FROM plugin_pay_transfer_bill b
            INNER JOIN plugin_pay_transfer_batch batch
                    ON batch.batch_no = b.batch_no
                   AND batch.is_deleted = 0
            WHERE b.is_deleted = 0
              AND b.status = 'PENDING'
              AND batch.status <> 'STOPPED'
            ORDER BY b.create_time ASC, b.seq_no ASC
            LIMIT #{limit}
            """)
    List<PaymentTransferBill> selectDispatchablePending(@Param("limit") int limit);

    /**
     * 统计全局占用额度金额（按发起时间 dispatched_time，缺省回退 create_time）。
     *
     * @param fromInclusive 起始时间（含）
     * @param toExclusive   结束时间（不含）
     * @return 金额合计（分）
     */
    @Select("""
            SELECT COALESCE(SUM(amount_cents), 0)
            FROM plugin_pay_transfer_bill
            WHERE is_deleted = 0
              AND status IN ('ACCEPTED','PROCESSING','WAIT_USER_CONFIRM','TRANSFERING','SUCCESS')
              AND COALESCE(dispatched_time, create_time) >= #{fromInclusive}
              AND COALESCE(dispatched_time, create_time) < #{toExclusive}
            """)
    long sumQuotaAmount(
            @Param("fromInclusive") LocalDateTime fromInclusive,
            @Param("toExclusive") LocalDateTime toExclusive);

    /**
     * 统计指定用户占用额度金额（按发起时间）。
     *
     * @param openid        收款人 openid
     * @param fromInclusive 起始时间（含）
     * @param toExclusive   结束时间（不含）
     * @return 金额合计（分）
     */
    @Select("""
            SELECT COALESCE(SUM(amount_cents), 0)
            FROM plugin_pay_transfer_bill
            WHERE is_deleted = 0
              AND openid = #{openid}
              AND status IN ('ACCEPTED','PROCESSING','WAIT_USER_CONFIRM','TRANSFERING','SUCCESS')
              AND COALESCE(dispatched_time, create_time) >= #{fromInclusive}
              AND COALESCE(dispatched_time, create_time) < #{toExclusive}
            """)
    long sumUserQuotaAmount(
            @Param("openid") String openid,
            @Param("fromInclusive") LocalDateTime fromInclusive,
            @Param("toExclusive") LocalDateTime toExclusive);
}
