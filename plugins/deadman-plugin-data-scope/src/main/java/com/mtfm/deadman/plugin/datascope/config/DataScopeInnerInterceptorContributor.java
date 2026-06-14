package com.mtfm.deadman.plugin.datascope.config;

import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.mtfm.deadman.core.mybatis.MybatisPlusInnerInterceptorContributor;
import com.mtfm.deadman.plugin.datascope.handler.DeadmanMultiDataPermissionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 向 MyBatis-Plus 拦截器链注册数据权限内部拦截器。
 */
@Component
@RequiredArgsConstructor
public class DataScopeInnerInterceptorContributor implements MybatisPlusInnerInterceptorContributor {

    private final DeadmanMultiDataPermissionHandler dataPermissionHandler;

    @Override
    public InnerInterceptor interceptor() {
        return new DataPermissionInterceptor(dataPermissionHandler);
    }

    @Override
    public int order() {
        return 0;
    }
}
