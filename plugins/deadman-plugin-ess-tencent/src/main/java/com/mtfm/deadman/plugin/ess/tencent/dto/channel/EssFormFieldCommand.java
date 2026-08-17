package com.mtfm.deadman.plugin.ess.tencent.dto.channel;

/**
 * 渠道版模板表单控件填充值。
 *
 * @param componentName 控件名称（与模板控件 ComponentName 对应）
 * @param componentValue 控件填充值
 */
public record EssFormFieldCommand(
        String componentName,
        String componentValue) {
}
