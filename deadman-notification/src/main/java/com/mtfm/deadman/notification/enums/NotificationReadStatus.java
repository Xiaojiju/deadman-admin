package com.mtfm.deadman.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 站内信阅读状态。
 */
@Getter
@RequiredArgsConstructor
public enum NotificationReadStatus {

    /** 未读 */
    UNREAD(0),
    /** 已读 */
    READ(1);

    private final int value;
}
