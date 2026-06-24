package com.mtfm.deadman.plugin.logistics.spi.track;

/**
 * 单条物流轨迹节点。
 *
 * @param time          原始时间字符串
 * @param formattedTime 格式化时间（若有）
 * @param context       轨迹描述
 * @param status        节点状态码（快递100 statusCode）
 * @param location      当前地点
 * @param areaName      行政区域
 */
public record LogisticsTrackNode(
        String time, String formattedTime, String context, String status, String location, String areaName) {
}
