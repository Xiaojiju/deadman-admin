package com.mtfm.deadman.component.client.enums;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;

/**
 * 用户端站内信分类，与消息中心三个页签对齐。
 */
public enum ClientNotificationCategory {

    /** 系统 */
    SYSTEM,

    /** 政策 */
    POLICY,

    /** 订单 */
    ORDER;

    /**
     * 解析筛选值。空表示不限分类。
     *
     * @param raw 请求里的分类
     * @return 分类，未传时为 null
     */
    public static ClientNotificationCategory from(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "消息分类只能是 SYSTEM、POLICY 或 ORDER");
        }
    }
}
