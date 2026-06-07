package com.mtfm.deadman.core.component;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.core.component.vo.DeadmanComponentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 组件目录 API：告知客户端当前服务端已装配哪些 components 模块。
 */
@RestController
@RequestMapping("/api/components")
@RequiredArgsConstructor
public class DeadmanComponentController {

    private final DeadmanComponentCatalogService deadmanComponentCatalogService;

    /**
     * 获取已装配组件列表（无需认证）。
     *
     * @return 组件目录
     */
    @GetMapping
    public Result<List<DeadmanComponentVO>> listComponents() {
        return Result.ok(deadmanComponentCatalogService.listComponents());
    }
}
