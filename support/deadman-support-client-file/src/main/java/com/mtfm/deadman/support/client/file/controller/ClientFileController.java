package com.mtfm.deadman.support.client.file.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mtfm.deadman.common.auth.AuthRealm;
import com.mtfm.deadman.common.auth.RequireAuth;
import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.plugin.file.dto.ResolveFileUrlsRequest;
import com.mtfm.deadman.plugin.file.service.FileService;
import com.mtfm.deadman.plugin.file.support.FileErrorCodes;
import com.mtfm.deadman.plugin.file.vo.FileAccessUrlVO;
import com.mtfm.deadman.plugin.file.vo.FileDownloadResource;
import com.mtfm.deadman.plugin.file.vo.FileMetadataVO;
import com.mtfm.deadman.support.client.file.access.SensitiveFilePolicy;
import com.mtfm.deadman.support.client.file.web.BinaryErrorResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 用户端文件上传与访问地址解析 API，桥接 file 插件与 client 鉴权。
 * <p>敏感证件类文件（身份证、人脸照、营业执照等，见 {@link SensitiveFilePolicy}）只有上传者本人能拿到地址和内容，
 * 地址是需要带 Authorization 头的 {@code /client/api/files/{fileId}/content}；其他人和不存在的文件一样返回
 * 40300「文件不存在或无权访问」，不暴露文件是否存在。公开文件照旧返回静态地址。
 */
@RestController
@RequestMapping("/client/api/files")
@RequiredArgsConstructor
public class ClientFileController {

    private static final String NO_ACCESS = "文件不存在或无权访问";

    private final FileService fileService;
    private final SensitiveFilePolicy sensitiveFilePolicy;

    /**
     * 用户端上传文件，默认使用 file 插件配置的存储 Provider（可切换 OSS/COS）。
     *
     * @param file       上传文件
     * @param bizType    业务分类（须已注册，如 rent、spare-part）
     * @param providerId 存储 Provider，为空时使用默认
     * @param loginUser  当前登录用户
     * @return 文件元数据（accessUrl 为当前可访问地址；敏感证件类为本人鉴权地址）
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireAuth(AuthRealm.CLIENT)
    public Result<FileMetadataVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bizType") String bizType,
            @RequestParam(value = "providerId", required = false) String providerId,
            @AuthenticationPrincipal ClientLoginUser loginUser) {
        return Result.ok(forClient(fileService.upload(file, bizType, providerId, loginUser.getUserId())));
    }

    /**
     * 按文件 ID 查询元数据（accessUrl 按存储 Provider 动态刷新）。敏感证件类只有上传者本人能查。
     *
     * @param fileId 文件主键
     * @param loginUser 当前登录用户
     * @return 文件元数据
     */
    @GetMapping("/{fileId}")
    @RequireAuth(AuthRealm.CLIENT)
    public Result<FileMetadataVO> getById(@PathVariable Long fileId,
            @AuthenticationPrincipal ClientLoginUser loginUser) {
        return Result.ok(forClient(requireReadable(fileId, loginUser.getUserId())));
    }

    /**
     * 按文件 ID 换取当前可访问 URL。敏感证件类只有上传者本人能换，拿到的是本人鉴权地址。
     *
     * @param fileId 文件主键
     * @param loginUser 当前登录用户
     * @return 访问地址
     */
    @GetMapping("/{fileId}/url")
    @RequireAuth(AuthRealm.CLIENT)
    public Result<FileAccessUrlVO> resolveUrl(@PathVariable Long fileId,
            @AuthenticationPrincipal ClientLoginUser loginUser) {
        return Result.ok(toUrl(forClient(requireReadable(fileId, loginUser.getUserId()))));
    }

    /**
     * 批量按文件 ID 换取当前可访问 URL。任一文件不存在或是别人的敏感证件类文件时整批返回 40300。
     *
     * @param request 文件 ID 列表
     * @param loginUser 当前登录用户
     * @return 访问地址列表（与入参顺序一致）
     */
    @PostMapping("/urls")
    @RequireAuth(AuthRealm.CLIENT)
    public Result<List<FileAccessUrlVO>> resolveUrls(@Valid @RequestBody ResolveFileUrlsRequest request,
            @AuthenticationPrincipal ClientLoginUser loginUser) {
        Map<Long, FileMetadataVO> found = fileService.findByIdsAsMap(request.fileIds());
        List<FileAccessUrlVO> result = new ArrayList<>();
        for (Long fileId : request.fileIds()) {
            if (fileId == null) {
                continue;
            }
            FileMetadataVO meta = found.get(fileId);
            if (meta == null || !readable(meta, loginUser.getUserId())) {
                throw new BusinessException(ResultCode.FORBIDDEN, NO_ACCESS + "：" + fileId);
            }
            result.add(toUrl(forClient(meta)));
        }
        return Result.ok(result);
    }

    /**
     * 读取文件内容，需要带 Authorization 头。敏感证件类文件的本人查看地址就是这个接口；响应不缓存。
     * <p>出错时返回非 2xx（响应体仍是 Result JSON）：fileId 格式不对 400，未登录 401，无权访问或不存在 404。
     *
     * @param fileIdText 文件主键
     * @param loginUser 当前登录用户
     * @return 文件流
     */
    @GetMapping("/{fileId}/content")
    @RequireAuth(AuthRealm.CLIENT)
    @BinaryErrorResponse
    public ResponseEntity<InputStreamResource> content(@PathVariable("fileId") String fileIdText,
            @AuthenticationPrincipal ClientLoginUser loginUser) {
        Long fileId = parseFileId(fileIdText);
        requireReadable(fileId, loginUser.getUserId());
        FileDownloadResource resource = fileService.openDownload(fileId);
        MediaType mediaType = resource.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(resource.getContentType());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore().cachePrivate())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(mediaType)
                .contentLength(resource.getSizeBytes() == null ? -1 : resource.getSizeBytes())
                .body(new InputStreamResource(resource.getInputStream()));
    }

    private static Long parseFileId(String text) {
        try {
            return Long.valueOf(text.trim());
        } catch (RuntimeException ex) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "fileId 格式不正确");
        }
    }

    private FileMetadataVO requireReadable(Long fileId, Long userId) {
        FileMetadataVO meta;
        try {
            meta = fileService.getById(fileId);
        } catch (BusinessException ex) {
            if (ex.getCode() == FileErrorCodes.FILE_NOT_FOUND) {
                throw new BusinessException(ResultCode.FORBIDDEN, NO_ACCESS);
            }
            throw ex;
        }
        if (!readable(meta, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, NO_ACCESS);
        }
        return meta;
    }

    private boolean readable(FileMetadataVO meta, Long userId) {
        return !sensitiveFilePolicy.isSensitive(meta.bizType())
                || (userId != null && userId.equals(meta.uploaderUserId()));
    }

    /**
     * 敏感证件类文件的 accessUrl 换成本人鉴权地址，不给静态直链。
     */
    private FileMetadataVO forClient(FileMetadataVO meta) {
        if (!sensitiveFilePolicy.isSensitive(meta.bizType())) {
            return meta;
        }
        return new FileMetadataVO(meta.id(), meta.fileCode(), meta.originalFilename(), meta.contentType(),
                meta.sizeBytes(), meta.providerId(), sensitiveFilePolicy.clientContentUrl(meta.id()), meta.bizType(),
                meta.uploaderUserId(), meta.createTime());
    }

    private static FileAccessUrlVO toUrl(FileMetadataVO meta) {
        return new FileAccessUrlVO(meta.id(), meta.accessUrl(), meta.providerId());
    }
}
