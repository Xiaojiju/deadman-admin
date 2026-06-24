package com.mtfm.deadman.common.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 业务响应码定义，可按模块扩展。
 */
@Getter
@RequiredArgsConstructor
public enum ResultCode {

    SUCCESS(0, "成功"),
    BAD_REQUEST(40000, "请求参数错误"),
    UNAUTHORIZED(40100, "未认证"),
    FORBIDDEN(40300, "无权限"),
    NOT_FOUND(40400, "资源不存在"),
    CONFLICT(40900, "资源冲突"),
    INTERNAL_ERROR(50000, "系统内部错误"),

    USER_NOT_FOUND(10001, "用户不存在"),
    USER_DISABLED(10002, "用户已禁用"),
    ACCOUNT_EXISTS(10003, "账号已存在"),
    ACCOUNT_NOT_FOUND(10004, "账号不存在"),
    PASSWORD_MISMATCH(10005, "用户名或密码错误"),
    PASSWORD_NOT_SET(10006, "未设置密码"),
    USER_SUPER_ADMIN_PROTECTED(10007, "超级管理员用户不允许删除或停用"),
    TOKEN_INVALID(10008, "令牌无效或已过期"),
    TOKEN_REUSE_DETECTED(10009, "检测到令牌异常重用，请重新登录"),

    ROLE_NOT_FOUND(11001, "角色不存在"),
    ROLE_CODE_EXISTS(11002, "角色编码已存在"),
    ROLE_SYSTEM_PROTECTED(11003, "系统内置角色不允许删除或禁用"),
    ROLE_SUPER_ADMIN_PROTECTED(11004, "超级管理员角色不允许修改权限"),
    PERMISSION_INVALID(11005, "存在无效的权限码"),

    DEPARTMENT_NOT_FOUND(12001, "部门不存在"),
    DEPARTMENT_CODE_EXISTS(12002, "部门编码已存在"),
    DEPARTMENT_HAS_CHILDREN(12003, "存在下级部门，无法删除"),
    DEPARTMENT_HAS_USERS(12004, "部门下存在用户，无法删除"),
    POSITION_NOT_FOUND(12011, "职位不存在"),
    POSITION_CODE_EXISTS(12012, "职位编码已存在"),
    POSITION_HAS_USERS(12013, "职位下存在用户，无法删除"),
    POSITION_DEPT_MISMATCH(12014, "职位所属部门与用户部门不一致"),
    PHONE_EXISTS(12021, "手机号已被其他用户绑定"),

    OAUTH_ALREADY_BOUND(12031, "该第三方账号已被其他用户绑定"),
    WECHAT_BIND_TOKEN_INVALID(12032, "微信绑定临时令牌无效或已过期"),

    FILE_NOT_FOUND(13001, "文件不存在"),
    FILE_TOO_LARGE(13002, "文件大小超出限制"),
    FILE_STORAGE_ERROR(13003, "文件存储失败"),
    FILE_PROVIDER_NOT_FOUND(13004, "文件存储 Provider 不存在"),
    FILE_BIZ_TYPE_UNREGISTERED(13005, "文件业务分类未注册"),

    WECHAT_PAY_ORDER_NOT_FOUND(14001, "支付单不存在"),
    WECHAT_PAY_OPENID_REQUIRED(14002, "缺少付款人 openid"),
    WECHAT_PAY_PREPAY_FAILED(14003, "微信预下单失败"),
    WECHAT_PAY_CONFIG_INVALID(14004, "微信支付配置无效"),

    PAY_PROVIDER_NOT_FOUND(14101, "支付 Provider 不存在"),
    PAY_ORDER_NOT_FOUND(14102, "支付单不存在"),
    PAY_NOTIFY_PARSE_FAILED(14103, "支付回调解析失败"),
    PAY_QUERY_FAILED(14104, "支付查单失败"),

    LOGISTICS_PROVIDER_NOT_FOUND(14201, "物流 Provider 不存在"),
    LOGISTICS_TRACK_QUERY_FAILED(14202, "快递轨迹查询失败"),
    LOGISTICS_CONFIG_INVALID(14203, "物流插件配置无效"),
    LOGISTICS_CARRIER_DETECT_FAILED(14204, "快递公司识别失败"),
    LOGISTICS_SUBSCRIBE_FAILED(14205, "快递轨迹订阅失败"),
    LOGISTICS_SUBSCRIBE_PUSH_INVALID(14206, "订阅推送验签失败"),
    LOGISTICS_WAYBILL_FAILED(14207, "电子面单操作失败"),
    LOGISTICS_SHIP_ORDER_FAILED(14208, "寄件下单失败"),
    LOGISTICS_SHIP_CANCEL_FAILED(14209, "寄件取消失败"),
    LOGISTICS_CARRIER_CODE_UNKNOWN(14210, "快递公司编码未注册或不支持当前渠道");

    private final int code;
    private final String message;
}
