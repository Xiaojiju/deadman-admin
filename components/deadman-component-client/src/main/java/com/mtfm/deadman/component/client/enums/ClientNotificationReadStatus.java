package com.mtfm.deadman.component.client.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 用户端站内信阅读状态。
 */
@Getter
@RequiredArgsConstructor
public enum ClientNotificationReadStatus {

    /** 未读 */
    UNREAD(0),

    /** 已读 */
    READ(1);

    /** 状态值 */
    private final int value;
}
