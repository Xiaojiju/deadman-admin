package com.mtfm.deadman.support.client.file.biztype;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mtfm.deadman.plugin.file.biztype.FileBizTypeContributor;
import com.mtfm.deadman.support.client.file.spi.SensitiveFileBizTypeContributor;

/**
 * 用户端常用文件业务分类贡献者，供 C 端上传接口校验 bizType。其中商户营业执照 merchant-license 属于敏感证件类。
 */
@Component
public class ClientFileBizTypeContributor implements FileBizTypeContributor, SensitiveFileBizTypeContributor {

    /**
     * 贡献工程信息平台 C 端常用业务分类。
     *
     * @return 业务分类编码列表
     */
    @Override
    public Collection<String> contribute() {
        return List.of("merchant-license", "avatar", "banner");
    }

    /**
     * 商户营业执照只给上传者本人看。
     *
     * @return 敏感分类
     */
    @Override
    public Collection<String> sensitiveBizTypes() {
        return List.of("merchant-license");
    }
}
