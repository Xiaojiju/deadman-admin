package com.mtfm.deadman.plugin.identity.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * 仅当腾讯云 SecretId 与 SecretKey 都已配置时创建真实人脸客户端。
 */
public class OnTencentIaiCredentialsCondition implements Condition {

    /**
     * 判断密钥是否齐全。
     *
     * @param context 条件上下文
     * @param metadata 注解元数据
     * @return 两个密钥都非空时为 true
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String secretId = context.getEnvironment().getProperty("deadman.plugin.identity.tencent.secret-id");
        String secretKey = context.getEnvironment().getProperty("deadman.plugin.identity.tencent.secret-key");
        return StringUtils.hasText(secretId) && StringUtils.hasText(secretKey);
    }
}
