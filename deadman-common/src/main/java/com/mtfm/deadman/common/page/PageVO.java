package com.mtfm.deadman.common.page;

import java.util.List;

/**
 * 统一分页响应结构。
 *
 * @param <T>      列表元素类型
 * @param records  当前页数据
 * @param total    总记录数
 * @param current  当前页码
 * @param size     每页条数
 */
public record PageVO<T>(List<T> records, long total, long current, long size) {

    /**
     * 根据分页参数构建分页响应。
     *
     * @param records   当前页数据
     * @param total     总记录数
     * @param pageParam 分页请求参数
     * @param <T>       列表元素类型
     * @return 分页响应
     */
    public static <T> PageVO<T> of(List<T> records, long total, PageParam pageParam) {
        return new PageVO<>(records, total, pageParam.resolvedCurrent(), pageParam.resolvedSize());
    }
}
