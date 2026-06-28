package com.mtfm.deadman.component.openauth.constant;

/**
 * 开放授权组件常量。
 */
public final class OpenAuthConstants {

    /** JWT claim：开放授权 realm 标识 */
    public static final String JWT_REALM = "OPEN";

    /** 授权类型：授权码模式 */
    public static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

    /** 开放 API 前缀 */
    public static final String OPEN_API_PREFIX = "/open-api";

    /** OAuth Token 端点路径 */
    public static final String TOKEN_ENDPOINT = "/open-api/oauth/token";

    /** Redis auth_code key 前缀 */
    public static final String CODE_KEY_PREFIX = "deadman:open-auth:code:";

    /** 应用状态：启用 */
    public static final int APP_STATUS_ENABLED = 1;

    /** 应用状态：禁用 */
    public static final int APP_STATUS_DISABLED = 0;

    private OpenAuthConstants() {
    }
}
