package com.mtfm.deadman.plugin.file.controller;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.plugin.file.service.FileService;
import com.mtfm.deadman.plugin.file.vo.FileDownloadResource;
import com.mtfm.deadman.plugin.file.vo.FileMetadataVO;
import com.mtfm.deadman.security.LoginUser;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传与下载 API。
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 上传文件。
     *
     * @param file       上传文件
     * @param bizType    业务分类
     * @param providerId 存储 Provider，为空时使用默认
     * @param loginUser  当前登录用户
     * @return 文件元数据
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.file.permission.FilePermissions).UPLOAD)")
    public Result<FileMetadataVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bizType", required = false) String bizType,
            @RequestParam(value = "providerId", required = false) String providerId,
            @AuthenticationPrincipal LoginUser loginUser) {
        Long uploaderUserId = loginUser == null ? null : loginUser.getUserId();
        return Result.ok(fileService.upload(file, bizType, providerId, uploaderUserId));
    }

    /**
     * 查询文件元数据。
     *
     * @param fileId 文件主键
     * @return 文件元数据
     */
    @GetMapping("/{fileId}")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.file.permission.FilePermissions).READ)")
    public Result<FileMetadataVO> getById(@PathVariable Long fileId) {
        return Result.ok(fileService.getById(fileId));
    }

    /**
     * 下载文件。
     *
     * @param fileId 文件主键
     * @return 文件流响应
     */
    @GetMapping("/{fileId}/download")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.file.permission.FilePermissions).DOWNLOAD)")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long fileId) throws Exception {
        FileDownloadResource resource = fileService.openDownload(fileId);
        String encodedFilename = URLEncoder.encode(resource.getOriginalFilename(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        MediaType mediaType = resource.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(resource.getContentType());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(mediaType)
                .contentLength(resource.getSizeBytes() == null ? -1 : resource.getSizeBytes())
                .body(new InputStreamResource(resource.getInputStream()));
    }

    /**
     * 删除文件。
     *
     * @param fileId 文件主键
     * @return 空响应
     */
    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.file.permission.FilePermissions).DELETE)")
    public Result<Void> delete(@PathVariable Long fileId) {
        fileService.delete(fileId);
        return Result.ok();
    }

    /**
     * 列出已注册的存储 Provider。
     *
     * @return Provider 标识列表
     */
    @GetMapping("/providers")
    @PreAuthorize("hasAuthority(T(com.mtfm.deadman.plugin.file.permission.FilePermissions).READ)")
    public Result<List<String>> listProviders() {
        return Result.ok(fileService.listProviders());
    }
}
