package com.mtfm.deadman.plugin.logistics.kuaidi100.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;

import com.kuaidi100.sdk.response.QueryTrackData;
import com.kuaidi100.sdk.response.QueryTrackResp;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsSubscribePushPayload;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackNode;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryContext;
import com.mtfm.deadman.plugin.logistics.spi.track.LogisticsTrackQueryResult;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * 快递100 轨迹数据与 SPI 结果映射工具。
 */
public final class Kuaidi100TrackMapper {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private Kuaidi100TrackMapper() {
    }

    /**
     * 将 SDK 轨迹节点映射为 SPI 节点。
     *
     * @param data SDK 轨迹节点
     * @return SPI 轨迹节点
     */
    public static LogisticsTrackNode toNode(QueryTrackData data) {
        if (data == null) {
            return null;
        }
        return new LogisticsTrackNode(
                data.getTime(),
                data.getFtime(),
                data.getContext(),
                data.getStatusCode(),
                data.getLocation(),
                data.getAreaName());
    }

    /**
     * 批量映射轨迹节点。
     *
     * @param dataList SDK 轨迹列表
     * @return SPI 轨迹节点列表
     */
    public static List<LogisticsTrackNode> toNodes(List<QueryTrackData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return List.of();
        }
        List<LogisticsTrackNode> nodes = new ArrayList<>(dataList.size());
        for (QueryTrackData data : dataList) {
            nodes.add(toNode(data));
        }
        return Collections.unmodifiableList(nodes);
    }

    /**
     * 将实时查单响应映射为 SPI 结果。
     *
     * @param providerId Provider 标识
     * @param context    查单上下文
     * @param response   SDK 查单响应
     * @return SPI 查单结果
     */
    public static LogisticsTrackQueryResult toQueryResult(
            String providerId, LogisticsTrackQueryContext context, QueryTrackResp response) {
        boolean signed = response != null && "1".equals(response.getIscheck());
        List<LogisticsTrackNode> nodes = response == null ? List.of() : toNodes(response.getData());
        String message = response == null
                ? null
                : (StringUtils.hasText(response.getMessage()) ? response.getMessage() : response.getStatus());
        return new LogisticsTrackQueryResult(
                providerId,
                response != null && StringUtils.hasText(response.getCom()) ? response.getCom() : context.carrierCode(),
                response != null && StringUtils.hasText(response.getNu()) ? response.getNu() : context.trackingNo(),
                response == null ? null : response.getState(),
                signed,
                message,
                nodes);
    }

    /**
     * 从订阅推送原始 param JSON 解析 lastResult 并映射为推送载荷。
     *
     * @param providerId Provider 标识
     * @param rawParam   原始 param JSON
     * @return 推送载荷；解析失败返回 null
     */
    public static LogisticsSubscribePushPayload parseSubscribePushPayload(String providerId, String rawParam) {
        try {
            JsonNode root = JSON_MAPPER.readTree(rawParam);
            JsonNode lastResultNode = root.get("lastResult");
            if (lastResultNode == null || lastResultNode.isNull()) {
                return null;
            }
            QueryTrackResp lastResult = JSON_MAPPER.treeToValue(lastResultNode, QueryTrackResp.class);
            return toSubscribePushPayload(providerId, rawParam, lastResult);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 将 lastResult 映射为订阅推送载荷。
     *
     * @param providerId Provider 标识
     * @param rawParam   原始 param JSON
     * @param lastResult 推送中的 lastResult
     * @return 推送载荷
     */
    public static LogisticsSubscribePushPayload toSubscribePushPayload(
            String providerId, String rawParam, QueryTrackResp lastResult) {
        if (lastResult == null) {
            return null;
        }
        boolean signed = "1".equals(lastResult.getIscheck());
        String message = StringUtils.hasText(lastResult.getMessage()) ? lastResult.getMessage() : lastResult.getStatus();
        return new LogisticsSubscribePushPayload(
                providerId,
                lastResult.getCom(),
                lastResult.getNu(),
                lastResult.getState(),
                signed,
                message,
                toNodes(lastResult.getData()),
                rawParam);
    }
}
