package com.mtfm.deadman.extension.flow;

/**
 * 流程元素执行失败包装异常，携带最内层（或当前层）元素 ID，便于错误处理器与日志定位。
 */
public final class FlowElementException extends RuntimeException {

    /** 出错元素 ID */
    private final String elementId;

    /**
     * @param elementId 元素 ID
     * @param cause 原始异常
     */
    public FlowElementException(String elementId, Throwable cause) {
        super("流程元素执行失败: " + elementId, cause);
        this.elementId = elementId;
    }

    /**
     * @return 元素 ID
     */
    public String getElementId() {
        return elementId;
    }

    /**
     * 沿 cause 链取最内层 {@link FlowElementException} 的 elementId；若无则返回 fallback。
     *
     * @param error 异常
     * @param fallback 回退 ID
     * @return 用于上报的元素 ID
     */
    public static String resolveElementId(Throwable error, String fallback) {
        String resolved = fallback;
        for (Throwable cursor = error; cursor != null; cursor = cursor.getCause()) {
            if (cursor instanceof FlowElementException fee) {
                resolved = fee.getElementId();
            }
        }
        return resolved;
    }

    /**
     * 若已是本类型则原样返回，否则包装。
     *
     * @param elementId 元素 ID
     * @param error 原始异常
     * @return FlowElementException
     */
    public static FlowElementException wrap(String elementId, Throwable error) {
        if (error instanceof FlowElementException fee) {
            return fee;
        }
        return new FlowElementException(elementId, error);
    }
}
