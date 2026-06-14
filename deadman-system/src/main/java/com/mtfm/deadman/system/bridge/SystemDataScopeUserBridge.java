package com.mtfm.deadman.system.bridge;

import com.mtfm.deadman.common.spi.DataScopeUserBridge;
import com.mtfm.deadman.system.entity.UserBase;
import com.mtfm.deadman.system.service.UserBaseService;
import com.mtfm.deadman.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 数据权限插件访问用户资料的 system 侧实现。
 */
@Component
@RequiredArgsConstructor
public class SystemDataScopeUserBridge implements DataScopeUserBridge {

    private final UserBaseService userBaseService;
    private final UserService userService;

    @Override
    public void requireExists(Long userId) {
        userBaseService.requireById(userId);
    }

    @Override
    public Long findDepartmentId(Long userId) {
        UserBase userBase = userService.getById(userId);
        return userBase == null ? null : userBase.getDepartmentId();
    }
}
