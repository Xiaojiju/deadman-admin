package com.mtfm.deadman.plugin.pay.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtfm.deadman.plugin.pay.entity.PaymentRefundOrder;

/**
 * 支付退款单 Mapper。
 */
@Mapper
public interface PaymentRefundOrderMapper extends BaseMapper<PaymentRefundOrder> {

    /**
     * 统计未终态（PROCESSING/ABNORMAL）退款占用金额合计。
     *
     * @param outTradeNo 平台支付单号
     * @return 占用金额（分）
     */
    @Select("""
            SELECT COALESCE(SUM(amount_refund), 0)
            FROM plugin_pay_refund
            WHERE out_trade_no = #{outTradeNo}
              AND status IN ('PROCESSING', 'ABNORMAL')
              AND is_deleted = 0
            """)
    int sumOutstandingAmount(@Param("outTradeNo") String outTradeNo);

    /**
     * CAS 标记已发起异常退款（仅未处理时成功）。
     *
     * @param outRefundNo 平台退款单号
     * @return 影响行数，1 表示抢占成功
     */
    @Update("""
            UPDATE plugin_pay_refund
            SET abnormal_handled = 1
            WHERE out_refund_no = #{outRefundNo}
              AND status = 'ABNORMAL'
              AND abnormal_handled = 0
              AND is_deleted = 0
            """)
    int casMarkAbnormalHandled(@Param("outRefundNo") String outRefundNo);

    /**
     * 清除异常退款处理标记（渠道调用失败后允许重试）。
     *
     * @param outRefundNo 平台退款单号
     * @return 影响行数
     */
    @Update("""
            UPDATE plugin_pay_refund
            SET abnormal_handled = 0
            WHERE out_refund_no = #{outRefundNo}
              AND abnormal_handled = 1
              AND is_deleted = 0
            """)
    int clearAbnormalHandled(@Param("outRefundNo") String outRefundNo);
}
