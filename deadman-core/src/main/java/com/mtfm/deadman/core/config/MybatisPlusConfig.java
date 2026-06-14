package com.mtfm.deadman.core.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.mtfm.deadman.core.mybatis.MybatisPlusInnerInterceptorContributor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置：数据权限（插件贡献）、分页、乐观锁。
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 组装 MyBatis-Plus 拦截器链。
     *
     * @param contributors 插件贡献的内部拦截器（如数据权限），插入在分页之前
     * @return 拦截器链
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(
            ObjectProvider<MybatisPlusInnerInterceptorContributor> contributors) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        contributors.orderedStream()
                .map(MybatisPlusInnerInterceptorContributor::interceptor)
                .forEach(interceptor::addInnerInterceptor);
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
