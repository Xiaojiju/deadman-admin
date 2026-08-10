package com.mtfm.deadman.plugin.pay.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtfm.deadman.plugin.pay.entity.PaymentTransferBatch;

/**
 * 商家转账批次 Mapper。
 */
@Mapper
public interface PaymentTransferBatchMapper extends BaseMapper<PaymentTransferBatch> {
}
