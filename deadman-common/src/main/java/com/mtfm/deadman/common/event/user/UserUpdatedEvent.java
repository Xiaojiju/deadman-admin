package com.mtfm.deadman.common.event.user;

/**
 * 用户更新领域事件。
 *
 * @param userId            用户 ID
 * @param departmentChanged 部门是否变更
 */
public record UserUpdatedEvent(Long userId, boolean departmentChanged) {
}
