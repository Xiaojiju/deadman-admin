package com.mtfm.deadman.system.permission;

/**
 * 系统内置模块权限码常量。
 */
public final class SystemPermissions {

    private SystemPermissions() {
    }

    /** 认证与账号 */
    public static final class Auth {
        public static final String PASSWORD_CHANGE = "auth:password:change";
        public static final String PERMISSIONS_READ = "auth:permissions:read";

        private Auth() {
        }
    }

    /** 用户管理 */
    public static final class User {
        public static final String LIST_READ = "user:list:read";
        public static final String CREATE = "user:create";
        public static final String UPDATE = "user:update";
        public static final String DELETE = "user:delete";
        public static final String PASSWORD_RESET = "user:password:reset";
        public static final String PROFILE_READ = "user:profile:read";
        public static final String PROFILE_UPDATE = "user:profile:update";

        private User() {
        }
    }

    /** 组织与职位 */
    public static final class Org {
        public static final String DEPT_LIST_READ = "dept:list:read";
        public static final String DEPT_CREATE = "dept:create";
        public static final String DEPT_UPDATE = "dept:update";
        public static final String DEPT_DELETE = "dept:delete";
        public static final String POSITION_LIST_READ = "position:list:read";
        public static final String POSITION_CREATE = "position:create";
        public static final String POSITION_UPDATE = "position:update";
        public static final String POSITION_DELETE = "position:delete";

        private Org() {
        }
    }

    /** 角色与权限 */
    public static final class Role {
        public static final String LIST_READ = "role:list:read";
        public static final String CREATE = "role:create";
        public static final String UPDATE = "role:update";
        public static final String DELETE = "role:delete";
        public static final String PERMISSION_ASSIGN = "role:permission:assign";
        public static final String USER_ASSIGN = "role:user:assign";

        private Role() {
        }
    }
}
