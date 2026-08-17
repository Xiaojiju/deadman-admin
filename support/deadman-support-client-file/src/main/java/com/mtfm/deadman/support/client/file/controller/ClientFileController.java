package com.mtfm.deadman.support.client.file.controller;

import java.util.List;

import org.springframework.http.MediaType;
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
import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.plugin.file.dto.ResolveFileUrlsRequest;
import com.mtfm.deadman.plugin.file.service.FileService;
import com.mtfm.deadman.plugin.file.vo.FileAccessUrlVO;
import com.mtfm.deadman.plugin.file.vo.FileMetadataVO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 用户端文件上传与访问地址解析 API，桥接 file 插件与 client 鉴权。
 */
@RestController
@RequestMapping("/client/api/files")
@RequiredArgsConstructor
public class ClientFileController {

    private final FileService fileService;

    /**
     * 用户端上传文件，默认使用 file 插件配置的存储 Provider（可切换 OSS/COS）。
     *
     * @param file       上传文件
     * @param bizType    业务分类（须已注册，如 rent、spare-part）
     * @param providerId 存储 Provider，为空时使用默认
     * @param loginUser  当前登录用户
     * @return 文件元数据（accessUrl 为当前可访问地址）
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireAuth(AuthRealm.CLIENT)
    public Result<FileMetadataVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bizType") String bizType,
            @RequestParam(value = "providerId", required = false) String providerId,
            @AuthenticationPrincipal ClientLoginUser loginUser) {
        return Result.ok(fileService.upload(file, bizType, providerId, loginUser.getUserId()));
    }

    /**
     * 按文件 ID 查询元数据（accessUrl 按存储 Provider 动态刷新）。
     *
     * @param fileId 文件主键
     * @return 文件元数据
     */
    @GetMapping("/{fileId}")
    @RequireAuth(AuthRealm.CLIENT)
    public Result<FileMetadataVO> getById(@PathVariable Long fileId) {
        return Result.ok(fileService.getById(fileId));
    }

    /**
     * 按文件 ID 换取当前可访问 URL。
     *
     * @param fileId 文件主键
     * @return 访问地址
     */
    @GetMapping("/{fileId}/url")
    @RequireAuth(AuthRealm.CLIENT)
    public Result<FileAccessUrlVO> resolveUrl(@PathVariable Long fileId) {
        return Result.ok(fileService.resolveAccessUrl(fileId));
    }

    /**
     * 批量按文件 ID 换取当前可访问 URL。
     *
     * @param request 文件 ID 列表
     * @return 访问地址列表（与入参顺序一致）
     */
    @PostMapping("/urls")
    @RequireAuth(AuthRealm.CLIENT)
    public Result<List<FileAccessUrlVO>> resolveUrls(@Valid @RequestBody ResolveFileUrlsRequest request) {
        return Result.ok(fileService.resolveAccessUrls(request.fileIds()));
    }
}
