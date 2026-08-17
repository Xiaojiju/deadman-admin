package com.mtfm.deadman.component.client.dto;

import com.mtfm.deadman.common.page.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端站内信收件箱分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientNotificationInboxPageQuery extends PageParam {

    /** 阅读状态：0-未读 1-已读，null 表示全部 */
    private Integer readStatus;
}
