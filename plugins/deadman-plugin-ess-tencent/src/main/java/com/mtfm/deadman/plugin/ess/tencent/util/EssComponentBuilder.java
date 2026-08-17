package com.mtfm.deadman.plugin.ess.tencent.util;

import org.springframework.util.StringUtils;

import com.mtfm.deadman.plugin.ess.tencent.dto.EssSignComponentCommand;
import com.tencentcloudapi.ess.v20201111.models.Component;

/**
 * 电子签签署控件构建工具。
 */
public final class EssComponentBuilder {

    private EssComponentBuilder() {
    }

    /**
     * 按坐标模式构建签名控件。
     *
     * @param componentType 控件类型
     * @param componentValue 控件值
     * @param posX X 坐标
     * @param posY Y 坐标
     * @param width 宽度
     * @param height 高度
     * @param fileIndex 文件序号
     * @param page 页码
     * @return 控件
     */
    public static Component buildCoordinate(
            String componentType,
            String componentValue,
            float posX,
            float posY,
            float width,
            float height,
            long fileIndex,
            long page) {
        Component component = new Component();
        component.setComponentType(componentType);
        if (StringUtils.hasText(componentValue)) {
            component.setComponentValue(componentValue);
        }
        component.setComponentPosX(posX);
        component.setComponentPosY(posY);
        component.setComponentWidth(width);
        component.setComponentHeight(height);
        component.setFileIndex(fileIndex);
        component.setComponentPage(page);
        return component;
    }

    /**
     * 将命令转换为 SDK Component。
     *
     * @param command 控件命令
     * @return SDK 控件
     */
    public static Component fromCommand(EssSignComponentCommand command) {
        Component component = new Component();
        component.setComponentType(command.componentType());
        if (StringUtils.hasText(command.componentValue())) {
            component.setComponentValue(command.componentValue());
        }
        if (command.componentPosX() != null) {
            component.setComponentPosX(command.componentPosX());
        }
        if (command.componentPosY() != null) {
            component.setComponentPosY(command.componentPosY());
        }
        if (command.componentWidth() != null) {
            component.setComponentWidth(command.componentWidth());
        }
        if (command.componentHeight() != null) {
            component.setComponentHeight(command.componentHeight());
        }
        component.setFileIndex(command.fileIndex() == null ? 0L : command.fileIndex());
        if (command.componentPage() != null) {
            component.setComponentPage(command.componentPage());
        }
        if (StringUtils.hasText(command.generateMode())) {
            component.setGenerateMode(command.generateMode());
        }
        if (StringUtils.hasText(command.componentId())) {
            component.setComponentId(command.componentId());
        }
        if (command.offsetX() != null) {
            component.setOffsetX(command.offsetX());
        }
        if (command.offsetY() != null) {
            component.setOffsetY(command.offsetY());
        }
        return component;
    }
}
