package com.mtfm.deadman.support.client.file.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 用户端 file 桥接模块配置。
 */
@Data
@ConfigurationProperties(prefix = "deadman.support.client-file")
public class ClientFileSupportProperties {

    /** 是否启用 C 端文件上传桥接 */
    private boolean enabled = true;

    /**
     * 额外的敏感证件类业务分类。各模块也可以实现 SensitiveFileBizTypeContributor 声明。
     */
    private List<String> sensitiveBizTypes = new ArrayList<>();

    /**
     * 管理端敏感文件签名地址的密钥。不配置时每次启动随机生成，重启后旧地址失效；多实例部署必须配置成相同值。
     */
    private String signedUrlSecret;

    /**
     * 管理端敏感文件签名地址的有效期（秒），最少 60，默认 30 分钟。
     */
    private long signedUrlTtlSeconds = 1800;
}
