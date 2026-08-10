package com.mtfm.deadman.extension.flow;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;

/**
 * 流程上下文基类：整条执行链上节点间共享的可变状态容器。
 * <p>
 * 约定：
 * <ul>
 *   <li>业务领域字段放在子类（如订单、用户 ID、中间结果）</li>
 *   <li>横切/临时扩展优先写入 {@link #attrs}</li>
 *   <li>并行网关<strong>禁止</strong>直接共享本实例：须覆盖 {@link #forkForParallelBranch()}
 *       为每条分支提供隔离副本（默认实现直接拒绝并行）</li>
 *   <li>节点 Bean 应保持无请求间可变状态（单例安全）</li>
 * </ul>
 */
public abstract class FlowContext {

    /**
     * 是否中断后续元素推进（并发安全）。
     */
    private final AtomicBoolean aborted = new AtomicBoolean(false);

    /**
     * 中断原因说明（volatile 保证跨线程可见）。
     */
    private volatile String abortReason;

    /**
     * 扩展属性袋（并发安全 Map）。
     */
    private final ConcurrentHashMap<String, Object> attrs = new ConcurrentHashMap<>();

    /**
     * 是否已中断。
     *
     * @return true 表示已中断
     */
    public boolean isAborted() {
        return aborted.get();
    }

    /**
     * 直接设置中断标记。
     *
     * @param aborted 是否中断
     */
    public void setAborted(boolean aborted) {
        this.aborted.set(aborted);
    }

    /**
     * @return 中断原因，可能为 null
     */
    public String getAbortReason() {
        return abortReason;
    }

    /**
     * 设置中断原因（优先使用 {@link #abort(String)}）。
     *
     * @param abortReason 原因
     */
    public void setAbortReason(String abortReason) {
        this.abortReason = abortReason;
    }

    /**
     * 中断流程并记录原因。
     *
     * @param reason 中断原因
     */
    public void abort(String reason) {
        this.abortReason = reason;
        this.aborted.set(true);
    }

    /**
     * 写入扩展属性（value 不可为 null）。
     *
     * @param key 键
     * @param value 值
     */
    public void putAttr(String key, Object value) {
        attrs.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
    }

    /**
     * 读取扩展属性。
     *
     * @param key 键
     * @param type 期望类型
     * @param <T> 返回类型
     * @return 值或 null
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
     * @return 扩展属性只读视图
     */
    public Map<String, Object> attrsView() {
        return Collections.unmodifiableMap(attrs);
    }

    /**
     * 将基类状态（abort/attrs）拷贝到并行分支副本。
     * <p>
     * 子类在 {@link #forkForParallelBranch()} 中创建副本后应调用本方法。
     *
     * @param branch 分支上下文
     */
    protected final void copyBaseStateTo(FlowContext branch) {
        Objects.requireNonNull(branch, "branch");
        branch.aborted.set(this.aborted.get());
        branch.abortReason = this.abortReason;
        branch.attrs.clear();
        branch.attrs.putAll(this.attrs);
    }

    /**
     * 为并行网关的一条分支创建隔离上下文。
     * <p>
     * <strong>默认拒绝并行</strong>：未覆盖本方法的 Context 不得使用 {@code ParallelGateway}，
     * 以避免共享可变业务字段导致的部分成功与数据竞争。
     * <p>
     * 覆盖要求：
     * <ul>
     *   <li>必须返回<strong>新实例</strong>，不得返回 {@code this}</li>
     *   <li>只读输入可共享不可变引用；分支写入不得直接写回父实例字段</li>
     *   <li>调用 {@link #copyBaseStateTo(FlowContext)} 同步 abort/attrs</li>
     * </ul>
     *
     * @return 分支专用上下文
     */
    public FlowContext forkForParallelBranch() {
        throw new BusinessException(ResultCode.BAD_REQUEST,
                "当前 FlowContext 未实现 forkForParallelBranch，禁止使用并行网关");
    }
}
