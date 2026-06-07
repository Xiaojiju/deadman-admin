package com.mtfm.deadman.notification.dto;

import com.mtfm.deadman.common.page.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 已发送通知分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationSentPageQuery extends PageParam {

    /** 标题关键词 */
    private String keyword;
}
