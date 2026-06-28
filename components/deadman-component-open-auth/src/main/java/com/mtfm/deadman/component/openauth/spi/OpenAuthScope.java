package com.mtfm.deadman.component.openauth.spi;

import java.util.List;
import java.util.Map;

/**
 * 开放授权范围，由业务 SPI 解析后写入 token。
 *
 * @param permissions 权限码或 scope 列表
 * @param extensions  扩展信息，如 dataScope
 */
public record OpenAuthScope(List<String> permissions, Map<String, Object> extensions) {
}
