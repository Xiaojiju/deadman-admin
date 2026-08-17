package com.mtfm.deadman.plugin.pay.dto.transfer;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.mtfm.deadman.common.page.PageParam;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端转账批次分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TransferBatchPageQuery extends PageParam {

    /** 批次号，可选，精确匹配 */
    private String batchNo;

    /** 业务单号，可选，精确匹配 */
    private String bizOrderNo;

    /** 批次状态，可选，精确匹配 */
    private String status;

    /** 收款 openid，可选，精确匹配 */
    private String openid;

    /** Provider 标识，可选，精确匹配 */
    private String providerId;

    /** 创建时间起（含） */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createTimeFrom;

    /** 创建时间止（含） */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createTimeTo;
}
