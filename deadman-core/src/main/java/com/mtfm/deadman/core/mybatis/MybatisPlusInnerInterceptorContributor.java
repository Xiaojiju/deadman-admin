package com.mtfm.deadman.core.mybatis;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;

/**
 * MyBatis-Plus 内部拦截器贡献 SPI，由插件注册并在分页拦截器之前插入。
 */
public interface MybatisPlusInnerInterceptorContributor {

    /**
     * 拦截器实例。
     *
     * @return 内部拦截器
     */
    InnerInterceptor interceptor();

    /**
     * 排序值，越小越靠前（数据权限应早于分页）。
     *
     * @return 顺序值
     */
    default int order() {
        return 0;
    }
}
