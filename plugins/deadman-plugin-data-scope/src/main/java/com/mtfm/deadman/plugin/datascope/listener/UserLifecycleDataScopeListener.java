package com.mtfm.deadman.plugin.datascope.listener;

import com.mtfm.deadman.common.event.user.UserCreatedEvent;
import com.mtfm.deadman.common.event.user.UserCreationSource;
import com.mtfm.deadman.common.event.user.UserDeletedEvent;
import com.mtfm.deadman.common.event.user.UserUpdatedEvent;
import com.mtfm.deadman.plugin.datascope.model.DataScopeType;
import com.mtfm.deadman.plugin.datascope.service.DataScopeSessionCache;
import com.mtfm.deadman.plugin.datascope.service.UserDataScopeProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 用户生命周期事件监听：维护数据权限配置与缓存。
 */
@Component
@RequiredArgsConstructor
public class UserLifecycleDataScopeListener {

    private final UserDataScopeProfileService profileService;
    private final DataScopeSessionCache sessionCache;

    /**
     * 用户创建后初始化数据权限配置。
     *
     * @param event 用户创建事件
     */
    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        if (event.userId() == null) {
            return;
        }
        if (event.source() == UserCreationSource.BOOTSTRAP_SUPER_ADMIN) {
            profileService.assignScope(event.userId(), DataScopeType.ALL, null);
            return;
        }
        profileService.assignDefaultScope(event.userId());
    }

    /**
     * 用户更新后刷新缓存（部门变更时重新解析可见范围）。
     *
     * @param event 用户更新事件
     */
    @EventListener
    public void onUserUpdated(UserUpdatedEvent event) {
        if (event.userId() == null || !event.departmentChanged()) {
            return;
        }
        sessionCache.refresh(event.userId());
    }

    /**
     * 用户删除后清理数据权限配置与缓存。
     *
     * @param event 用户删除事件
     */
    @EventListener
    public void onUserDeleted(UserDeletedEvent event) {
        if (event.userId() == null) {
            return;
        }
        profileService.removeByUserId(event.userId());
    }
}
