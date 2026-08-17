package com.mtfm.deadman.plugin.ess.tencent.dto;

/**
 * 签署控件定位命令。
 *
 * @param componentType 控件类型，如 SIGN_SIGNATURE / SIGN_SEAL / TEXT
 * @param componentValue 控件值（静默签印章 Id 等，可空）
 * @param componentPosX 控件 X 坐标（坐标定位模式）
 * @param componentPosY 控件 Y 坐标（坐标定位模式）
 * @param componentWidth 控件宽度
 * @param componentHeight 控件高度
 * @param fileIndex 文件序号，通常为 0
 * @param componentPage 页码，从 1 开始
 * @param generateMode 定位模式：空/NORMAL 坐标；KEYWORD 关键字；FIELD 表单域
 * @param componentId 关键字或表单域名（KEYWORD/FIELD 模式必填）
 * @param offsetX 关键字偏移 X
 * @param offsetY 关键字偏移 Y
 */
public record EssSignComponentCommand(
        String componentType,
        String componentValue,
        Float componentPosX,
        Float componentPosY,
        Float componentWidth,
        Float componentHeight,
        Long fileIndex,
        Long componentPage,
        String generateMode,
        String componentId,
        Float offsetX,
        Float offsetY) {
}
