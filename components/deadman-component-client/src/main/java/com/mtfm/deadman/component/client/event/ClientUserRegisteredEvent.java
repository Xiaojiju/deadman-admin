package com.mtfm.deadman.component.client.event;

/**
 * 用户端新用户注册/首登创建完成事件。
 *
 * @param userId     新用户 ID
 * @param inviteCode 可选推广码（扫码 scene {@code p.{promoCode}} 或裸推广码）
 */
public record ClientUserRegisteredEvent(Long userId, String inviteCode) {
}
