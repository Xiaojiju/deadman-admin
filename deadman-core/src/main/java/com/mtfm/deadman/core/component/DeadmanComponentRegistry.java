package com.mtfm.deadman.core.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 运行时组件注册表，聚合所有已装配的 {@link DeadmanComponentDescriptor}。
 */
@Slf4j
@Component
public class DeadmanComponentRegistry implements SmartInitializingSingleton {

    private final ObjectProvider<DeadmanComponentDescriptor> descriptorBeans;
    private final ObjectProvider<DeadmanComponentContributor> contributors;
    private final List<DeadmanComponentDescriptor> components = new CopyOnWriteArrayList<>();

    public DeadmanComponentRegistry(
            ObjectProvider<DeadmanComponentDescriptor> descriptorBeans,
            ObjectProvider<DeadmanComponentContributor> contributors) {
        this.descriptorBeans = descriptorBeans;
        this.contributors = contributors;
    }

    /**
     * 所有单例初始化完成后收集组件描述符。
     */
    @Override
    public void afterSingletonsInstantiated() {
        descriptorBeans.forEach(this::register);
        contributors.forEach(contributor -> register(contributor.descriptor()));
        components.sort(Comparator.comparingInt(DeadmanComponentDescriptor::order)
                .thenComparing(DeadmanComponentDescriptor::code));
        log.info("Deadman 组件注册完成，共 {} 个: {}", components.size(), components.stream()
                .map(DeadmanComponentDescriptor::code)
                .toList());
    }

    /**
     * 返回当前已注册组件的只读快照。
     *
     * @return 组件列表
     */
    public List<DeadmanComponentDescriptor> list() {
        return List.copyOf(components);
    }

    private void register(DeadmanComponentDescriptor descriptor) {
        if (descriptor == null || !StringUtils.hasText(descriptor.code())) {
            log.warn("忽略无效组件描述符: {}", descriptor);
            return;
        }
        boolean duplicated = components.stream().anyMatch(existing -> existing.code().equals(descriptor.code()));
        if (duplicated) {
            log.warn("组件编码重复，跳过后者: {}", descriptor.code());
            return;
        }
        components.add(descriptor);
    }
}
