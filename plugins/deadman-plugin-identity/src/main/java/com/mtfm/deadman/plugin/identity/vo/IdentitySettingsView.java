package com.mtfm.deadman.plugin.identity.vo;

/**
 * 认证开关，供业务侧决定是否展示实名入口。注册接口本身不强制实名。
 *
 * @param enabled 插件是否装配
 * @param realNameEnabled 是否允许个人实名
 * @param enterpriseEnabled 是否允许企业认证
 * @param requiredOnRegister 注册时是否要求实名（仅开关，不拦截注册）
 * @param matchScoreThreshold 同一人最低分
 * @param livenessScoreThreshold 活体最低分
 */
public record IdentitySettingsView(boolean enabled, boolean realNameEnabled, boolean enterpriseEnabled,
    boolean requiredOnRegister, float matchScoreThreshold, float livenessScoreThreshold) {
}
