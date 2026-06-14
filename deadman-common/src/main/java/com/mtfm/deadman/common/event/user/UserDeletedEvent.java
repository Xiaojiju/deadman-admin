package com.mtfm.deadman.common.event.user;

/**
 * 用户删除领域事件。
 *
 * @param userId 用户 ID
 */
public record UserDeletedEvent(Long userId) {
}
