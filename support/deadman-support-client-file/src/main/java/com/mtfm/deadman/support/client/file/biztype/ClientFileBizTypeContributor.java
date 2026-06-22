package com.mtfm.deadman.support.client.file.biztype;

import com.mtfm.deadman.plugin.file.biztype.FileBizTypeContributor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 用户端常用文件业务分类贡献者，供 C 端上传接口校验 bizType。
 */
@Component
public class ClientFileBizTypeContributor implements FileBizTypeContributor {

    /**
     * 贡献工程信息平台 C 端常用业务分类。
     *
     * @return 业务分类编码列表
     */
    @Override
    public Collection<String> contribute() {
        return List.of("rent", "spare-part", "merchant-license", "avatar");
    }
}
