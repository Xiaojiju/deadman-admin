package com.mtfm.deadman.common.event.user;

/**
 * 用户创建来源，供插件监听并执行初始化逻辑。
 */
public enum UserCreationSource {

    /** 自助注册 */
    REGISTER,

    /** 管理端创建 */
    ADMIN,

    /** 引导超级管理员 */
    BOOTSTRAP_SUPER_ADMIN
}
