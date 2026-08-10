package com.mtfm.deadman.plugin.pay.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtfm.deadman.plugin.pay.entity.PaymentOrder;

/**
 * 支付平台单 Mapper。
 */
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {

    /**
     * 原子累加已退金额并将支付单置为转入退款。
     *
     * @param outTradeNo           平台支付单号
     * @param refundAmount         本次成功退款金额（分）
     * @param status               目标状态（通常为 REFUND）
     * @param channelTransactionId 渠道支付单号，可空则不覆盖
     * @return 影响行数
     */
    @Update("""
            UPDATE plugin_pay_order
            SET amount_refunded = COALESCE(amount_refunded, 0) + #{refundAmount},
                status = #{status},
                channel_transaction_id = COALESCE(#{channelTransactionId}, channel_transaction_id),
                version = version + 1
            WHERE out_trade_no = #{outTradeNo}
              AND is_deleted = 0
              AND COALESCE(amount_refunded, 0) + #{refundAmount} <= amount_total
            """)
    int addRefundedAmount(
            @Param("outTradeNo") String outTradeNo,
            @Param("refundAmount") int refundAmount,
            @Param("status") String status,
            @Param("channelTransactionId") String channelTransactionId);
}
