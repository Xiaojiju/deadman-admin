package com.mtfm.deadman.common.event.user;

/**
 * 用户创建领域事件。
 *
 * @param userId 用户 ID
 * @param source 创建来源
 */
public record UserCreatedEvent(Long userId, UserCreationSource source) {
}
