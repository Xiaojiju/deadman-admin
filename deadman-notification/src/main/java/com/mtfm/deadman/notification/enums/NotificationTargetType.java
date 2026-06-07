package com.mtfm.deadman.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 站内信发送目标类型。
 */
@Getter
@RequiredArgsConstructor
public enum NotificationTargetType {

    /** 指定用户 */
    USER(1),
    /** 指定部门 */
    DEPARTMENT(2),
    /** 指定职位 */
    POSITION(3),
    /** 全体用户 */
    ALL(4);

    private final int value;

    public static NotificationTargetType fromValue(int value) {
        for (NotificationTargetType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知通知目标类型: " + value);
    }
}
