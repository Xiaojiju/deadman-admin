package com.mtfm.deadman.core.component;

import com.mtfm.deadman.core.component.vo.DeadmanComponentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 查询当前服务端已装配的 Deadman 组件目录。
 */
@Service
@RequiredArgsConstructor
public class DeadmanComponentCatalogService {

    private final DeadmanComponentRegistry deadmanComponentRegistry;

    /**
     * 列出所有已注册组件，供前端动态渲染模块入口。
     *
     * @return 组件展示列表
     */
    public List<DeadmanComponentVO> listComponents() {
        return deadmanComponentRegistry.list().stream().map(this::toVo).toList();
    }

    private DeadmanComponentVO toVo(DeadmanComponentDescriptor descriptor) {
        return new DeadmanComponentVO(
                descriptor.code(),
                descriptor.name(),
                descriptor.description(),
                descriptor.apiPrefix(),
                descriptor.order(),
                descriptor.uiHints());
    }
}
