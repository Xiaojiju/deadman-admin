package com.mtfm.deadman.plugin.ess.tencent.vo.channel;

/**
 * 创建子客控制台登录链接结果。
 *
 * @param consoleUrl 控制台/认证跳转链接或小程序 path
 * @param activated 子客企业是否已激活（实名完成）
 * @param operatorVerified 经办人是否已实名
 */
public record CreateConsoleLoginUrlResult(String consoleUrl, boolean activated, boolean operatorVerified) {
}
