package com.mtfm.deadman.extension.flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

/**
 * 流程上下文基类：节点间共享状态，支持中断与扩展属性。
 * <p>
 * 业务字段放在子类；横切扩展优先写入 {@link #attrs}，避免频繁改节点签名。
 */
@Getter
public abstract class FlowContext {

    /** 是否中断后续主节点 */
    @Setter
    private boolean aborted;

    /** 中断原因说明 */
    @Setter
    private String abortReason;

    /** 扩展属性 */
    private final Map<String, Object> attrs = new HashMap<>();

    /**
     * 中断流程并记录原因。
     *
     * @param reason 中断原因
     */
    public void abort(String reason) {
        this.aborted = true;
        this.abortReason = reason;
    }

    /**
     * 写入扩展属性。
     *
     * @param key 键
     * @param value 值
     */
    public void putAttr(String key, Object value) {
        attrs.put(Objects.requireNonNull(key, "key"), value);
    }

    /**
     * 读取扩展属性。
     *
     * @param key 键
     * @param type 期望类型
     * @param <T> 泛型
     * @return 属性值，不存在时为 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttr(String key, Class<T> type) {
        Object value = attrs.get(key);
        if (value == null) {
            return null;
        }
        return (T) value;
    }

    /**
     * 只读扩展属性视图。
     *
     * @return 属性 Map
     */
    public Map<String, Object> attrsView() {
        return Collections.unmodifiableMap(attrs);
    }
}
