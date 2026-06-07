package com.mtfm.deadman.common.page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 统一分页请求参数（query：{@code current}、{@code size}）。
 * <p>
 * 业务查询 DTO 可继承本类并追加筛选字段，避免在各接口重复声明页码与每页条数。
 */
@Data
public class PageParam {

    public static final long DEFAULT_CURRENT = 1L;
    public static final long DEFAULT_SIZE = 10L;
    public static final long MAX_SIZE = 100L;

    /** 当前页码，从 1 开始 */
    @Min(value = 1, message = "页码最小为 1")
    private Long current = DEFAULT_CURRENT;

    /** 每页条数，最大 100 */
    @Min(value = 1, message = "每页条数最小为 1")
    @Max(value = 100, message = "每页条数最大为 100")
    private Long size = DEFAULT_SIZE;

    public long resolvedCurrent() {
        return current == null || current < 1 ? DEFAULT_CURRENT : current;
    }

    public long resolvedSize() {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
