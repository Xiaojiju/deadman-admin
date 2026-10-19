package com.mtfm.deadman.support.client.file.access;

import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.plugin.file.controller.FileController;
import com.mtfm.deadman.plugin.file.vo.FileAccessUrlVO;
import com.mtfm.deadman.plugin.file.vo.FileMetadataVO;

import lombok.RequiredArgsConstructor;

/**
 * 管理端文件接口（/api/files/**）返回敏感证件类文件的本地地址时，换成带过期时间的签名地址，
 * 管理端页面照旧把 accessUrl 放进 img 即可显示。返回结构不变，只是地址后面多了 exp、sig 参数。
 */
@RestControllerAdvice(assignableTypes = FileController.class)
@RequiredArgsConstructor
public class AdminSensitiveFileUrlAdvice implements ResponseBodyAdvice<Object> {

    private final SensitiveFilePolicy policy;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
        Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
        ServerHttpResponse response) {
        if (body instanceof Result<?> result && result.getData() != null) {
            ((Result<Object>) result).setData(sign(result.getData()));
        }
        return body;
    }

    private Object sign(Object data) {
        if (data instanceof FileAccessUrlVO url) {
            return new FileAccessUrlVO(url.fileId(), policy.signIfSensitive(url.accessUrl()), url.providerId());
        }
        if (data instanceof FileMetadataVO meta) {
            return new FileMetadataVO(meta.id(), meta.fileCode(), meta.originalFilename(), meta.contentType(),
                meta.sizeBytes(), meta.providerId(), policy.signIfSensitive(meta.accessUrl()), meta.bizType(),
                meta.uploaderUserId(), meta.createTime());
        }
        if (data instanceof List<?> list) {
            return list.stream().map(this::sign).toList();
        }
        return data;
    }
}
