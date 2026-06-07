package com.mtfm.deadman.plugin.excel.autoconfigure;

import com.mtfm.deadman.plugin.excel.config.ExcelPluginProperties;
import com.mtfm.deadman.plugin.excel.service.DeadExcelService;
import com.mtfm.deadman.plugin.excel.service.DefaultDeadExcelService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Excel 工具包插件自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(ExcelPluginProperties.class)
@ConditionalOnProperty(prefix = "deadman.plugin.excel", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DeadmanExcelPluginAutoConfiguration {

    /**
     * 注册默认 Excel 导入导出服务。
     *
     * @return Excel 服务 Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public DeadExcelService deadExcelService() {
        return new DefaultDeadExcelService();
    }
}
